package com.kotlintexteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlintexteditor.ui.editor.CodeEditorView
import com.kotlintexteditor.ui.editor.EditorLanguage
import com.kotlintexteditor.ui.editor.EditorState
import com.kotlintexteditor.ui.editor.rememberEditorState
import com.kotlintexteditor.ui.theme.KotlinTextEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinTextEditorTheme {
                TextEditorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorApp() {
    val editorState = rememberEditorState(
        initialText = """// Welcome to Kotlin Text Editor
fun main() {
    println("Hello, World!")
    
    // TODO: Start coding your Kotlin project here
    val message = "This is a basic text editor"
    println(message)
}

class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }
    
    fun multiply(a: Int, b: Int): Int = a * b
}
"""
    )
    
    var currentEditorState by editorState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Kotlin Text Editor") 
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Add menu functionality */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add save functionality */ }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            StatusBar(editorState = currentEditorState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main editor area
            CodeEditorView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                initialText = currentEditorState.text,
                language = currentEditorState.language,
                onTextChanged = { newText ->
                    currentEditorState = EditorState.fromText(
                        text = newText,
                        filePath = currentEditorState.filePath
                    ).copy(isModified = true)
                }
            )
        }
    }
}

@Composable
fun StatusBar(editorState: EditorState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File info
            Text(
                text = editorState.filePath ?: "Untitled.kt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Lines: ${editorState.lineCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Words: ${editorState.wordCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Chars: ${editorState.characterCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Language indicator
                Text(
                    text = editorState.language.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TextEditorPreview() {
    KotlinTextEditorTheme {
        TextEditorApp()
    }
}