package com.kotlintexteditor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    initialText: String = "",
    language: EditorLanguage = EditorLanguage.KOTLIN,
    onTextChanged: (String) -> Unit = {},
    isReadOnly: Boolean = false
) {
    val context = LocalContext.current
    var editor by remember { mutableStateOf<CodeEditor?>(null) }

    AndroidView(
        factory = { ctx ->
            CodeEditor(ctx).apply {
                // Configure the editor
                setupEditor(this, language, isReadOnly)
                setText(initialText)
                editor = this
            }
        },
        modifier = modifier,
        update = { codeEditor ->
            // Update editor when composable recomposes
            if (codeEditor.text.toString() != initialText) {
                codeEditor.setText(initialText)
            }
            
            // Set up text change listener - will implement this later when we have proper API documentation
            // For now, we'll update on explicit user actions
            try {
                // Basic event handling - this might need adjustment based on actual Sora Editor API
            } catch (e: Exception) {
                // Handle any API differences gracefully
            }
        }
    )
}

private fun setupEditor(editor: CodeEditor, language: EditorLanguage, isReadOnly: Boolean) {
    // For now, we'll use a basic text language
    // Later we'll add proper syntax highlighting
    editor.setEditorLanguage(null) // Use default text editor language
    
    // Configure editor settings
    editor.apply {
        isEditable = !isReadOnly
        
        // Enable features
        getComponent(Magnifier::class.java).isEnabled = true
        
        // Configure appearance
        setLineNumberEnabled(true)
        setWordwrap(false)
        
        // Set color scheme (basic configuration)
        colorScheme = EditorColorScheme().apply {
            // We'll configure colors properly later when we add proper theme support
        }
        
        // Configure text size
        setTextSize(14f)
        
        // Configure cursor
        setCursorBlinkPeriod(500)
    }
}

enum class EditorLanguage {
    KOTLIN,
    JAVA,
    PLAIN_TEXT
}

// Extension function to get file extension
fun String.getFileExtension(): EditorLanguage {
    return when (this.substringAfterLast('.', "").lowercase()) {
        "kt", "kts" -> EditorLanguage.KOTLIN
        "java" -> EditorLanguage.JAVA
        else -> EditorLanguage.PLAIN_TEXT
    }
}

// Data class for editor state
@Stable
data class EditorState(
    val text: String = "",
    val language: EditorLanguage = EditorLanguage.KOTLIN,
    val filePath: String? = null,
    val isModified: Boolean = false,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val lineCount: Int = 1
) {
    companion object {
        fun fromText(text: String, filePath: String? = null): EditorState {
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val characters = text.length
            val lines = maxOf(1, text.count { it == '\n' } + 1)
            val language = filePath?.getFileExtension() ?: EditorLanguage.KOTLIN
            
            return EditorState(
                text = text,
                language = language,
                filePath = filePath,
                wordCount = words,
                characterCount = characters,
                lineCount = lines
            )
        }
    }
}

@Composable
fun rememberEditorState(
    initialText: String = "",
    filePath: String? = null
): MutableState<EditorState> {
    return remember(initialText, filePath) {
        mutableStateOf(EditorState.fromText(initialText, filePath))
    }
}
