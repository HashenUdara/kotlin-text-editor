package com.kotlintexteditor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    initialText: String = "",
    language: EditorLanguage = EditorLanguage.KOTLIN,
    onTextChanged: (String) -> Unit = {},
    onSelectionChanged: (Int, Int) -> Unit = { _, _ -> },
    isReadOnly: Boolean = false
) {
    val context = LocalContext.current
    val codeEditor = remember { CodeEditor(context) }

    DisposableEffect(codeEditor, language) {
        setupEditor(codeEditor, language, isReadOnly)
        onDispose {
            // Clean up resources if necessary
        }
    }

    // Update editor text when initialText changes from outside
    LaunchedEffect(initialText) {
        if (codeEditor.text.toString() != initialText) {
            codeEditor.setText(initialText)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            codeEditor.apply {
                // Set up text change listener
                subscribeEvent(ContentChangeEvent::class.java) { event, unsubscribe ->
                    val newText = codeEditor.text.toString()
                    onTextChanged(newText)
                }
                
                // Set up selection change listener
                subscribeEvent(SelectionChangeEvent::class.java) { event, unsubscribe ->
                    val startIndex = codeEditor.text.getCharIndex(event.left.line, event.left.column)
                    val endIndex = codeEditor.text.getCharIndex(event.right.line, event.right.column)
                    onSelectionChanged(startIndex, endIndex)
                }
                
                // Set initial text
                setText(initialText)
            }
        },
        update = { editor ->
            // Update editor when composable recomposes
            if (editor.text.toString() != initialText) {
                editor.setText(initialText)
            }
        }
    )
}

private fun setupEditor(editor: CodeEditor, language: EditorLanguage, isReadOnly: Boolean) {
    // Configure editor based on language type using configurable system
    val context = editor.context
    val configurableManager = com.kotlintexteditor.syntax.ConfigurableEditorManager.getInstance(context)
    
    val supportedLanguage = when (language) {
        EditorLanguage.KOTLIN -> com.kotlintexteditor.syntax.SupportedLanguage.KOTLIN
        EditorLanguage.JAVA -> com.kotlintexteditor.syntax.SupportedLanguage.JAVA
        EditorLanguage.PYTHON -> com.kotlintexteditor.syntax.SupportedLanguage.PYTHON
        EditorLanguage.JAVASCRIPT -> com.kotlintexteditor.syntax.SupportedLanguage.JAVASCRIPT
        EditorLanguage.TYPESCRIPT -> com.kotlintexteditor.syntax.SupportedLanguage.TYPESCRIPT
        EditorLanguage.CSHARP -> com.kotlintexteditor.syntax.SupportedLanguage.CSHARP
        EditorLanguage.CPP -> com.kotlintexteditor.syntax.SupportedLanguage.CPP
        EditorLanguage.HTML -> com.kotlintexteditor.syntax.SupportedLanguage.HTML
        EditorLanguage.CSS -> com.kotlintexteditor.syntax.SupportedLanguage.CSS
        EditorLanguage.JSON -> com.kotlintexteditor.syntax.SupportedLanguage.JSON
        EditorLanguage.XML -> com.kotlintexteditor.syntax.SupportedLanguage.XML
        EditorLanguage.YAML -> com.kotlintexteditor.syntax.SupportedLanguage.YAML
        EditorLanguage.MARKDOWN -> com.kotlintexteditor.syntax.SupportedLanguage.MARKDOWN
        EditorLanguage.PLAIN_TEXT -> com.kotlintexteditor.syntax.SupportedLanguage.PLAIN_TEXT
    }
    
    // Use configurable syntax highlighting
    configurableManager.configureEditor(editor, supportedLanguage)
    
    // Set read-only mode if needed
    editor.isEditable = !isReadOnly
}

enum class EditorLanguage {
    KOTLIN,
    JAVA,
    PYTHON,
    JAVASCRIPT,
    TYPESCRIPT,
    CSHARP,
    CPP,
    HTML,
    CSS,
    JSON,
    XML,
    YAML,
    MARKDOWN,
    PLAIN_TEXT
}

// Extension function to get file extension
fun String.getFileExtension(): EditorLanguage {
    return when (this.substringAfterLast('.', "").lowercase()) {
        "kt", "kts" -> EditorLanguage.KOTLIN
        "java" -> EditorLanguage.JAVA
        "py", "pyw" -> EditorLanguage.PYTHON
        "js", "mjs", "jsx" -> EditorLanguage.JAVASCRIPT
        "ts", "tsx" -> EditorLanguage.TYPESCRIPT
        "cs" -> EditorLanguage.CSHARP
        "cpp", "cxx", "cc", "c", "h", "hpp" -> EditorLanguage.CPP
        "html", "htm" -> EditorLanguage.HTML
        "css" -> EditorLanguage.CSS
        "json" -> EditorLanguage.JSON
        "xml" -> EditorLanguage.XML
        "yml", "yaml" -> EditorLanguage.YAML
        "md", "markdown" -> EditorLanguage.MARKDOWN
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
