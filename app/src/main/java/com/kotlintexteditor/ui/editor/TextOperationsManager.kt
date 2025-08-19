package com.kotlintexteditor.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages text operations like copy, paste, cut, undo, redo
 */
class TextOperationsManager(private val context: Context) {
    
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    // Undo/Redo stack
    private val undoStack = mutableListOf<TextState>()
    private val redoStack = mutableListOf<TextState>()
    private var currentUndoIndex = -1
    private val maxUndoHistory = 50
    
    // Operation state
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    private val _canPaste = MutableStateFlow(false)
    val canPaste: StateFlow<Boolean> = _canPaste.asStateFlow()
    
    /**
     * Represents a text state for undo/redo operations
     */
    data class TextState(
        val text: String,
        val selectionStart: Int = 0,
        val selectionEnd: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Result of a text operation
     */
    data class OperationResult(
        val success: Boolean,
        val newText: String = "",
        val newSelectionStart: Int = 0,
        val newSelectionEnd: Int = 0,
        val message: String? = null
    )
    
    init {
        // Monitor clipboard changes
        updatePasteAvailability()
    }
    
    /**
     * Save current text state for undo operations
     */
    fun saveState(text: String, selectionStart: Int = 0, selectionEnd: Int = 0) {
        val newState = TextState(text, selectionStart, selectionEnd)
        
        // Don't save duplicate states
        if (undoStack.isNotEmpty() && undoStack.last().text == text) {
            return
        }
        
        // Add to undo stack
        undoStack.add(newState)
        
        // Limit undo history size
        if (undoStack.size > maxUndoHistory) {
            undoStack.removeAt(0)
        }
        
        // Clear redo stack when new action is performed
        redoStack.clear()
        
        currentUndoIndex = undoStack.size - 1
        updateUndoRedoAvailability()
    }
    
    /**
     * Copy selected text to clipboard
     */
    fun copyText(selectedText: String): OperationResult {
        return try {
            val clip = ClipData.newPlainText("Copied Text", selectedText)
            clipboardManager.setPrimaryClip(clip)
            updatePasteAvailability()
            
            OperationResult(
                success = true,
                message = "Text copied to clipboard"
            )
        } catch (e: Exception) {
            OperationResult(
                success = false,
                message = "Failed to copy text: ${e.message}"
            )
        }
    }
    
    /**
     * Cut selected text (copy + delete)
     */
    fun cutText(
        fullText: String,
        selectionStart: Int,
        selectionEnd: Int
    ): OperationResult {
        return try {
            if (selectionStart == selectionEnd) {
                return OperationResult(
                    success = false,
                    message = "No text selected to cut"
                )
            }
            
            val selectedText = fullText.substring(selectionStart, selectionEnd)
            
            // Copy to clipboard
            val copyResult = copyText(selectedText)
            if (!copyResult.success) {
                return copyResult
            }
            
            // Remove selected text
            val newText = fullText.removeRange(selectionStart, selectionEnd)
            
            OperationResult(
                success = true,
                newText = newText,
                newSelectionStart = selectionStart,
                newSelectionEnd = selectionStart,
                message = "Text cut to clipboard"
            )
        } catch (e: Exception) {
            OperationResult(
                success = false,
                message = "Failed to cut text: ${e.message}"
            )
        }
    }
    
    /**
     * Paste text from clipboard
     */
    fun pasteText(
        fullText: String,
        selectionStart: Int,
        selectionEnd: Int
    ): OperationResult {
        return try {
            val clipData = clipboardManager.primaryClip
            if (clipData == null || clipData.itemCount == 0) {
                return OperationResult(
                    success = false,
                    message = "Clipboard is empty"
                )
            }
            
            val pastedText = clipData.getItemAt(0).text?.toString() ?: ""
            if (pastedText.isEmpty()) {
                return OperationResult(
                    success = false,
                    message = "No text in clipboard"
                )
            }
            
            // Replace selected text with pasted text
            val beforeSelection = fullText.substring(0, selectionStart)
            val afterSelection = fullText.substring(selectionEnd)
            val newText = beforeSelection + pastedText + afterSelection
            
            val newSelectionStart = selectionStart + pastedText.length
            
            OperationResult(
                success = true,
                newText = newText,
                newSelectionStart = newSelectionStart,
                newSelectionEnd = newSelectionStart,
                message = "Text pasted from clipboard"
            )
        } catch (e: Exception) {
            OperationResult(
                success = false,
                message = "Failed to paste text: ${e.message}"
            )
        }
    }
    
    /**
     * Undo last operation
     */
    fun undo(): OperationResult {
        if (!_canUndo.value || currentUndoIndex <= 0) {
            return OperationResult(
                success = false,
                message = "Nothing to undo"
            )
        }
        
        // Move current state to redo stack
        val currentState = undoStack[currentUndoIndex]
        redoStack.add(currentState)
        
        // Move to previous state
        currentUndoIndex--
        val previousState = undoStack[currentUndoIndex]
        
        updateUndoRedoAvailability()
        
        return OperationResult(
            success = true,
            newText = previousState.text,
            newSelectionStart = previousState.selectionStart,
            newSelectionEnd = previousState.selectionEnd,
            message = "Undo successful"
        )
    }
    
    /**
     * Redo last undone operation
     */
    fun redo(): OperationResult {
        if (!_canRedo.value || redoStack.isEmpty()) {
            return OperationResult(
                success = false,
                message = "Nothing to redo"
            )
        }
        
        val redoState = redoStack.removeAt(redoStack.size - 1)
        currentUndoIndex++
        
        if (currentUndoIndex >= undoStack.size) {
            undoStack.add(redoState)
        } else {
            undoStack[currentUndoIndex] = redoState
        }
        
        updateUndoRedoAvailability()
        
        return OperationResult(
            success = true,
            newText = redoState.text,
            newSelectionStart = redoState.selectionStart,
            newSelectionEnd = redoState.selectionEnd,
            message = "Redo successful"
        )
    }
    
    /**
     * Select all text
     */
    fun selectAll(text: String): OperationResult {
        return OperationResult(
            success = true,
            newText = text,
            newSelectionStart = 0,
            newSelectionEnd = text.length,
            message = "All text selected"
        )
    }
    
    /**
     * Clear undo/redo history
     */
    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        currentUndoIndex = -1
        updateUndoRedoAvailability()
    }
    
    /**
     * Update paste availability based on clipboard content
     */
    private fun updatePasteAvailability() {
        val hasClipboardData = try {
            val clipData = clipboardManager.primaryClip
            clipData != null && clipData.itemCount > 0 && 
            !clipData.getItemAt(0).text.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
        
        _canPaste.value = hasClipboardData
    }
    
    /**
     * Update undo/redo availability
     */
    private fun updateUndoRedoAvailability() {
        _canUndo.value = currentUndoIndex > 0
        _canRedo.value = redoStack.isNotEmpty()
    }
    
    /**
     * Get clipboard text preview (first 50 characters)
     */
    fun getClipboardPreview(): String? {
        return try {
            val clipData = clipboardManager.primaryClip
            val text = clipData?.getItemAt(0)?.text?.toString()
            if (text != null && text.length > 50) {
                text.substring(0, 50) + "..."
            } else {
                text
            }
        } catch (e: Exception) {
            null
        }
    }
}
