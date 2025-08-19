package com.kotlintexteditor.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kotlintexteditor.data.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TextEditorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val fileManager = FileManager(application)
    private val textOperationsManager = TextOperationsManager(application)
    
    // Editor state
    private val _editorState = MutableStateFlow(
        EditorState(
            text = """// Welcome to Kotlin Text Editor!
// This is a powerful code editor for Android
// 
// Features:
// - Syntax highlighting
// - File operations (Open, Save, New)
// - Find and Replace
// - Multiple language support
// 
fun main() {
    println("Hello, World!")
    
    val editor = TextEditor()
    editor.loadFile("example.kt")
}

class TextEditor {
    fun loadFile(fileName: String) {
        println("Loading file: " + fileName)
    }
    
    fun saveFile(content: String) {
        println("Saving file with " + content.length + " characters")
    }
}
""",
            language = EditorLanguage.KOTLIN
        )
    )
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()
    
    // UI state
    private val _uiState = MutableStateFlow(TextEditorUiState())
    val uiState: StateFlow<TextEditorUiState> = _uiState.asStateFlow()
    
    // Auto-save functionality
    private var autoSaveJob: kotlinx.coroutines.Job? = null
    
    // Text operations state
    val canUndo = textOperationsManager.canUndo
    val canRedo = textOperationsManager.canRedo
    val canPaste = textOperationsManager.canPaste
    
    // Selection state
    private val _selectionState = MutableStateFlow(SelectionState())
    val selectionState: StateFlow<SelectionState> = _selectionState.asStateFlow()
    
    /**
     * Update editor text content
     */
    fun updateText(newText: String, saveToHistory: Boolean = true) {
        val currentState = _editorState.value
        
        // Save to undo history if this is a user action
        if (saveToHistory && currentState.text != newText) {
            textOperationsManager.saveState(
                text = currentState.text,
                selectionStart = _selectionState.value.start,
                selectionEnd = _selectionState.value.end
            )
        }
        
        val updatedState = EditorState.fromText(
            text = newText,
            filePath = currentState.filePath
        ).copy(
            isModified = newText != currentState.text || currentState.isModified,
            language = currentState.language
        )
        
        _editorState.value = updatedState
        
        // Schedule auto-save if file exists
        scheduleAutoSave()
    }
    
    /**
     * Update text selection
     */
    fun updateSelection(start: Int, end: Int) {
        _selectionState.value = SelectionState(start, end)
    }
    
    /**
     * Open a file from URI
     */
    fun openFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = fileManager.readFile(uri)
            
            if (result.success) {
                val extension = fileManager.getFileExtension(uri)
                val language = extension.getFileExtension()
                
                _editorState.value = EditorState.fromText(
                    text = result.content,
                    filePath = result.fileName
                ).copy(
                    language = language,
                    isModified = false
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentFileUri = uri,
                    statusMessage = "File opened: ${result.fileName}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.error ?: "Failed to open file"
                )
            }
        }
    }
    
    /**
     * Save current content to a file
     */
    fun saveFile(uri: Uri? = null) {
        viewModelScope.launch {
            val targetUri = uri ?: _uiState.value.currentFileUri
            
            if (targetUri == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No file selected for saving"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            
            val result = fileManager.writeFile(targetUri, _editorState.value.text)
            
            if (result.success) {
                _editorState.value = _editorState.value.copy(isModified = false)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentFileUri = targetUri,
                    statusMessage = "File saved: ${result.fileName}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.error ?: "Failed to save file"
                )
            }
        }
    }
    
    /**
     * Create a new file
     */
    fun newFile() {
        _editorState.value = EditorState(
            text = "",
            language = EditorLanguage.KOTLIN,
            filePath = null,
            isModified = false
        )
        
        _uiState.value = _uiState.value.copy(
            currentFileUri = null,
            statusMessage = "New file created"
        )
        
        // Clear undo/redo history for new file
        textOperationsManager.clearHistory()
        _selectionState.value = SelectionState()
    }
    
    /**
     * Schedule auto-save if enabled and file exists
     */
    private fun scheduleAutoSave() {
        if (!_uiState.value.autoSaveEnabled || _uiState.value.currentFileUri == null) {
            return
        }
        
        // Cancel previous auto-save job
        autoSaveJob?.cancel()
        
        // Schedule new auto-save after 2 seconds of inactivity
        autoSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // 2 seconds
            if (_editorState.value.isModified) {
                saveFile()
            }
        }
    }
    
    /**
     * Toggle auto-save feature
     */
    fun toggleAutoSave() {
        val newValue = !_uiState.value.autoSaveEnabled
        _uiState.value = _uiState.value.copy(autoSaveEnabled = newValue)
        
        if (newValue) {
            scheduleAutoSave()
        } else {
            autoSaveJob?.cancel()
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear status message
     */
    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
    
    /**
     * Check if file can be saved
     */
    fun canSave(): Boolean {
        return _editorState.value.isModified && _uiState.value.currentFileUri != null
    }
    
    /**
     * Get current file name for display
     */
    fun getCurrentFileName(): String {
        return _editorState.value.filePath ?: "Untitled.kt"
    }
    
    // Text Operations
    
    /**
     * Copy selected text
     */
    fun copyText() {
        val selection = _selectionState.value
        if (!selection.hasSelection) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No text selected to copy"
            )
            return
        }
        
        val selectedText = _editorState.value.text.substring(selection.start, selection.end)
        val result = textOperationsManager.copyText(selectedText)
        
        _uiState.value = _uiState.value.copy(
            statusMessage = if (result.success) result.message else null,
            errorMessage = if (!result.success) result.message else null
        )
    }
    
    /**
     * Cut selected text
     */
    fun cutText() {
        val selection = _selectionState.value
        if (!selection.hasSelection) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No text selected to cut"
            )
            return
        }
        
        val result = textOperationsManager.cutText(
            fullText = _editorState.value.text,
            selectionStart = selection.start,
            selectionEnd = selection.end
        )
        
        if (result.success) {
            updateText(result.newText, saveToHistory = true)
            updateSelection(result.newSelectionStart, result.newSelectionEnd)
            _uiState.value = _uiState.value.copy(statusMessage = result.message)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = result.message)
        }
    }
    
    /**
     * Paste text from clipboard
     */
    fun pasteText() {
        val selection = _selectionState.value
        val result = textOperationsManager.pasteText(
            fullText = _editorState.value.text,
            selectionStart = selection.start,
            selectionEnd = selection.end
        )
        
        if (result.success) {
            updateText(result.newText, saveToHistory = true)
            updateSelection(result.newSelectionStart, result.newSelectionEnd)
            _uiState.value = _uiState.value.copy(statusMessage = result.message)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = result.message)
        }
    }
    
    /**
     * Undo last operation
     */
    fun undo() {
        val result = textOperationsManager.undo()
        
        if (result.success) {
            updateText(result.newText, saveToHistory = false)
            updateSelection(result.newSelectionStart, result.newSelectionEnd)
            _uiState.value = _uiState.value.copy(statusMessage = result.message)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = result.message)
        }
    }
    
    /**
     * Redo last undone operation
     */
    fun redo() {
        val result = textOperationsManager.redo()
        
        if (result.success) {
            updateText(result.newText, saveToHistory = false)
            updateSelection(result.newSelectionStart, result.newSelectionEnd)
            _uiState.value = _uiState.value.copy(statusMessage = result.message)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = result.message)
        }
    }
    
    /**
     * Select all text
     */
    fun selectAll() {
        val text = _editorState.value.text
        updateSelection(0, text.length)
        _uiState.value = _uiState.value.copy(statusMessage = "All text selected")
    }
    
    /**
     * Get clipboard preview for context menu
     */
    fun getClipboardPreview(): String? {
        return textOperationsManager.getClipboardPreview()
    }
    
    /**
     * Check if there is selected text
     */
    fun hasSelection(): Boolean {
        return _selectionState.value.hasSelection
    }
}

/**
 * UI state for the text editor
 */
data class TextEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val currentFileUri: Uri? = null,
    val autoSaveEnabled: Boolean = true,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

/**
 * Text selection state
 */
data class SelectionState(
    val start: Int = 0,
    val end: Int = 0
) {
    val hasSelection: Boolean
        get() = start != end
        
    val selectedLength: Int
        get() = kotlin.math.abs(end - start)
}
