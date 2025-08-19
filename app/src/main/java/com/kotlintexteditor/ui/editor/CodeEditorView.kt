package com.kotlintexteditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    text: String = "",
    onTextChange: (String) -> Unit = {},
    language: EditorLanguage = EditorLanguage.KOTLIN
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Update textFieldValue when external text changes
    LaunchedEffect(text) {
        if (textFieldValue.text != text) {
            textFieldValue = textFieldValue.copy(text = text)
        }
    }

    Row(
        modifier = modifier
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        // Line numbers
        LineNumbers(
            text = textFieldValue.text,
            modifier = Modifier
                .padding(end = 8.dp)
                .verticalScroll(verticalScrollState)
        )

        // Code editor
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onTextChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.White
                ),
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "// Start typing your ${language.name.lowercase()} code here...",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun LineNumbers(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split('\n')
    Column(
        modifier = modifier
            .background(Color(0xFF2D2D2D))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        lines.forEachIndexed { index, _ ->
            Text(
                text = "${index + 1}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color(0xFF858585)
                ),
                modifier = Modifier.padding(vertical = 0.dp)
            )
        }
    }
}

// Syntax highlighting function for Kotlin (basic implementation)
private fun applySyntaxHighlighting(text: String, language: EditorLanguage): AnnotatedString {
    return buildAnnotatedString {
        val keywords = when (language) {
            EditorLanguage.KOTLIN -> listOf(
                "fun", "val", "var", "class", "object", "interface", "if", "else", "when", "for", 
                "while", "do", "return", "import", "package", "public", "private", "protected", 
                "internal", "open", "final", "abstract", "override", "companion", "data", "sealed"
            )
            EditorLanguage.JAVA -> listOf(
                "public", "private", "protected", "static", "final", "abstract", "class", "interface",
                "extends", "implements", "if", "else", "switch", "case", "for", "while", "do", "return",
                "import", "package", "try", "catch", "finally", "throw", "throws"
            )
            EditorLanguage.PLAIN_TEXT -> emptyList()
        }

        append(text)
        
        // Simple keyword highlighting
        keywords.forEach { keyword ->
            val regex = "\\b$keyword\\b".toRegex()
            regex.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF569CD6), // Blue color for keywords
                        fontWeight = FontWeight.Bold
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }

        // String highlighting
        val stringRegex = "\".*?\"".toRegex()
        stringRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFFCE9178)), // Orange color for strings
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Comment highlighting
        val commentRegex = "//.*".toRegex()
        commentRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFF6A9955)), // Green color for comments
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}

enum class EditorLanguage {
    KOTLIN,
    JAVA,
    PLAIN_TEXT
}
