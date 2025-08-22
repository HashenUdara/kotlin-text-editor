package com.kotlintexteditor.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kotlintexteditor.data.FileManager
import com.kotlintexteditor.ui.dialogs.FileTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TextEditorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val fileManager = FileManager(application)
    private val textOperationsManager = TextOperationsManager(application)
    private val searchManager = SearchManager()
    private val enhancedLanguageManager = com.kotlintexteditor.syntax.EnhancedLanguageManager.getInstance(application)
    private val compilationService = com.kotlintexteditor.compilation.CompilationService.getInstance(application)

    // Track the original content when a file is opened for comparison
    private var originalFileContent: String = ""

    init {
        // Initialize enhanced syntax highlighting
        viewModelScope.launch {
            enhancedLanguageManager.initialize()
        }
    }
    
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
    
    // Search state
    val searchQuery = searchManager.searchQuery
    val replaceText = searchManager.replaceText
    val isCaseSensitive = searchManager.isCaseSensitive
    val isWholeWord = searchManager.isWholeWord
    val isRegexEnabled = searchManager.isRegexEnabled
    val searchResults = searchManager.searchResults
    val isSearchDialogVisible = searchManager.isDialogVisible
    
    // New File Dialog state
    private val _isNewFileDialogVisible = MutableStateFlow(false)
    val isNewFileDialogVisible: StateFlow<Boolean> = _isNewFileDialogVisible.asStateFlow()
    
    // File Browser Dialog state
    private val _isFileBrowserDialogVisible = MutableStateFlow(false)
    val isFileBrowserDialogVisible: StateFlow<Boolean> = _isFileBrowserDialogVisible.asStateFlow()
    
    // Language configuration dialog state
    private val _isLanguageConfigDialogVisible = MutableStateFlow(false)
    val isLanguageConfigDialogVisible: StateFlow<Boolean> = _isLanguageConfigDialogVisible.asStateFlow()
    
    // Recent files tracking
    private val _recentFiles = MutableStateFlow<List<com.kotlintexteditor.ui.dialogs.RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<com.kotlintexteditor.ui.dialogs.RecentFile>> = _recentFiles.asStateFlow()
    
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
            isModified = newText != originalFileContent,
            language = currentState.language
        )
        
        _editorState.value = updatedState
        
        // Update search manager with new text
        searchManager.updateText(newText)
        
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
                val language = result.fileName.getFileExtension()
                
                // Store the original content for comparison
                originalFileContent = result.content
                
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
                
                // Add to recent files
                addToRecentFiles(result.fileName, uri.toString(), result.content.length.toLong())
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
                // Update the original content since file is now saved
                originalFileContent = _editorState.value.text
                
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
     * Show new file dialog
     */
    fun showNewFileDialog() {
        _isNewFileDialogVisible.value = true
    }
    
    /**
     * Hide new file dialog
     */
    fun hideNewFileDialog() {
        _isNewFileDialogVisible.value = false
    }
    
    /**
     * Show file browser dialog
     */
    fun showFileBrowserDialog() {
        _isFileBrowserDialogVisible.value = true
    }
    
    /**
     * Hide file browser dialog
     */
    fun hideFileBrowserDialog() {
        _isFileBrowserDialogVisible.value = false
    }
    
    /**
     * Show language configuration dialog
     */
    fun showLanguageConfigDialog() {
        _isLanguageConfigDialogVisible.value = true
    }
    
    /**
     * Hide language configuration dialog
     */
    fun hideLanguageConfigDialog() {
        _isLanguageConfigDialogVisible.value = false
    }
    
    /**
     * Create a new file with specified language and content
     */
        fun createNewFile(language: EditorLanguage, fileName: String, template: FileTemplate) {
        val content = template.getContent(fileName)

        // Set original content as empty for new files (so any content is considered modified)
        originalFileContent = ""

        _editorState.value = EditorState(
            text = content,
            language = language,
            filePath = fileName,
            isModified = content.isNotEmpty(), // Mark as modified if template has content
            wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
            characterCount = content.length,
            lineCount = maxOf(1, content.count { it == '\n' } + 1)
        )
        
        _uiState.value = _uiState.value.copy(
            currentFileUri = null,
            statusMessage = "New ${language.name.lowercase()} file created: $fileName"
        )
        
        // Clear undo/redo history for new file
        textOperationsManager.clearHistory()
        _selectionState.value = SelectionState()
        
        // Hide dialog
        _isNewFileDialogVisible.value = false
    }
    
    /**
     * Create a new file and trigger save as dialog
     */
    fun createNewFileWithSaveDialog(language: EditorLanguage, fileName: String, template: FileTemplate): String {
        // Create the file content first
        createNewFile(language, fileName, template)
        
        // Return the suggested filename for the save dialog
        return fileName
    }
    
    /**
     * Quick new file creation (for backward compatibility)
     */
    fun newFile() {
        // Set original content as empty for new files
        originalFileContent = ""
        createNewFile(EditorLanguage.KOTLIN, "untitled.kt", FileTemplate.EMPTY)
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
    
    // Search and Replace Operations
    
    /**
     * Show find and replace dialog
     */
    fun showFindReplaceDialog() {
        searchManager.showDialog()
    }
    
    /**
     * Hide find and replace dialog
     */
    fun hideFindReplaceDialog() {
        searchManager.hideDialog()
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        searchManager.updateSearchQuery(query, _editorState.value.text)
    }
    
    /**
     * Update replace text
     */
    fun updateReplaceText(replace: String) {
        searchManager.updateReplaceText(replace)
    }
    
    /**
     * Update case sensitivity
     */
    fun updateCaseSensitive(caseSensitive: Boolean) {
        searchManager.updateCaseSensitive(caseSensitive)
    }
    
    /**
     * Update whole word matching
     */
    fun updateWholeWord(wholeWord: Boolean) {
        searchManager.updateWholeWord(wholeWord)
    }
    
    /**
     * Update regex enabled
     */
    fun updateRegexEnabled(regexEnabled: Boolean) {
        searchManager.updateRegexEnabled(regexEnabled)
    }
    
    /**
     * Find next match
     */
    fun findNext() {
        val result = searchManager.findNext()
        when (result) {
            is SearchResult.Found -> {
                updateSelection(result.match.startIndex, result.match.endIndex)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Match ${result.position} of ${result.total}"
                )
            }
            is SearchResult.NotFound -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }
    
    /**
     * Find previous match
     */
    fun findPrevious() {
        val result = searchManager.findPrevious()
        when (result) {
            is SearchResult.Found -> {
                updateSelection(result.match.startIndex, result.match.endIndex)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Match ${result.position} of ${result.total}"
                )
            }
            is SearchResult.NotFound -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }
    
    /**
     * Replace current match
     */
    fun replaceCurrent() {
        val result = searchManager.replaceCurrent()
        when (result) {
            is ReplaceResult.Success -> {
                updateText(result.newText, saveToHistory = true)
                updateSelection(result.newCursorPosition, result.newCursorPosition)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Replaced \"${result.replacedText}\""
                )
            }
            is ReplaceResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }
    
    /**
     * Replace all matches
     */
    fun replaceAll() {
        val result = searchManager.replaceAll()
        when (result) {
            is ReplaceResult.Success -> {
                updateText(result.newText, saveToHistory = true)
                updateSelection(result.newCursorPosition, result.newCursorPosition)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Replaced ${result.total} matches"
                )
                // Clear search after replace all
                searchManager.clearSearch()
            }
            is ReplaceResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message
                )
            }
        }
    }
    
    /**
     * Clear search
     */
    fun clearSearch() {
        searchManager.clearSearch()
    }
    
    /**
     * Get current search match for highlighting
     */
    fun getCurrentSearchMatch(): SearchMatch? {
        return searchManager.getCurrentMatch()
    }
    
    /**
     * Add file to recent files list
     */
    private fun addToRecentFiles(fileName: String, path: String, sizeBytes: Long) {
        val currentRecentFiles = _recentFiles.value.toMutableList()
        
        // Remove if already exists (to move to top)
        currentRecentFiles.removeAll { it.name == fileName }
        
        // Add to beginning of list
        val recentFile = com.kotlintexteditor.ui.dialogs.RecentFile.create(
            name = fileName,
            path = path,
            lastModifiedTimestamp = System.currentTimeMillis(),
            sizeBytes = sizeBytes,
            uri = path
        )
        currentRecentFiles.add(0, recentFile)
        
        // Keep only last 10 files
        if (currentRecentFiles.size > 10) {
            currentRecentFiles.removeAt(currentRecentFiles.size - 1)
        }
        
        _recentFiles.value = currentRecentFiles
    }
    
    /**
     * Open a recent file
     */
    fun openRecentFile(recentFile: com.kotlintexteditor.ui.dialogs.RecentFile) {
        recentFile.uri?.let { uriString ->
            try {
                val uri = android.net.Uri.parse(uriString)
                openFile(uri)
                hideFileBrowserDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to open recent file: ${recentFile.name}"
                )
            }
        }
    }
    
    /**
     * Clear recent files list
     */
    fun clearRecentFiles() {
        _recentFiles.value = emptyList()
    }
    
    // Compilation state and methods
    val compilationStatus = compilationService.compilationStatus
    val compilationResult = compilationService.compilationResult
    
    /**
     * Compile the current Kotlin code via ADB
     */
    fun compileCurrentCode() {
        val currentState = _editorState.value
        
        // Only compile Kotlin files
        if (currentState.language != EditorLanguage.KOTLIN) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Compilation is only supported for Kotlin files"
            )
            return
        }
        
        // Check if there's code to compile
        if (currentState.text.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No code to compile"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(statusMessage = "Starting compilation...")
                
                val fileName = currentState.filePath?.let { path ->
                    path.substringAfterLast("/").ifEmpty { "Main.kt" }
                } ?: "Main.kt"
                
                val result = compilationService.compileKotlinCode(
                    sourceCode = currentState.text,
                    fileName = fileName
                )
                
                when (result) {
                    is com.kotlintexteditor.compilation.CompilationResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            statusMessage = "Compilation successful: ${result.message}",
                            errorMessage = null
                        )
                    }
                    is com.kotlintexteditor.compilation.CompilationResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Compilation failed: ${result.message}",
                            statusMessage = null
                        )
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Compilation error: ${e.message}",
                    statusMessage = null
                )
            }
        }
    }
    
    /**
     * Clear compilation results
     */
    fun clearCompilationResult() {
        // Reset the compilation result
        viewModelScope.launch {
            compilationService.compilationResult.value.let {
                if (it != null) {
                    // Simply clear the status message to effectively clear the result display
                    _uiState.value = _uiState.value.copy(
                        statusMessage = null,
                        errorMessage = null
                    )
                }
            }
        }
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
