package com.kotlintexteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlintexteditor.ui.editor.CodeEditorView
import com.kotlintexteditor.ui.editor.EditorLanguage
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
    var editorText by remember { mutableStateOf("// Welcome to Kotlin Text Editor\nfun main() {\n    println(\"Hello, World!\")\n}") }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Kotlin Text Editor") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Add menu functionality */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status bar with character count
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Characters: ${editorText.length}")
                    Text("Words: ${editorText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size}")
                }
            }
            
            // Code Editor
            CodeEditorView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
                text = editorText,
                onTextChange = { newText ->
                    editorText = newText
                },
                language = EditorLanguage.KOTLIN
            )
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