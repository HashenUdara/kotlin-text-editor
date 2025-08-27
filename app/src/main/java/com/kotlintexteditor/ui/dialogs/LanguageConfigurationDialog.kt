package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlintexteditor.ui.editor.EditorLanguage
import com.kotlintexteditor.syntax.EnhancedLanguageManager
import com.kotlintexteditor.syntax.LanguageConfiguration
import kotlinx.coroutines.launch

/**
 * Simplified Language Configuration Dialog
 * Shows information about the current language support system
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigurationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val context = LocalContext.current
        val languageManager = remember { EnhancedLanguageManager.getInstance(context) }
        val scope = rememberCoroutineScope()
        
        var selectedLanguage by remember { mutableStateOf<EditorLanguage?>(null) }
        var showJsonEditor by remember { mutableStateOf(false) }
        var currentConfig by remember { mutableStateOf<LanguageConfiguration?>(null) }
        var jsonText by remember { mutableStateOf("") }
        var isJsonValid by remember { mutableStateOf(true) }
        var jsonErrorMessage by remember { mutableStateOf("") }
        
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Language Support",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (showJsonEditor && selectedLanguage != null) {
                        // JSON Editor View
                        JsonEditorView(
                            language = selectedLanguage!!,
                            jsonText = jsonText,
                            isValid = isJsonValid,
                            errorMessage = jsonErrorMessage,
                            onJsonChange = { newJson ->
                                jsonText = newJson
                                scope.launch {
                                    val result = languageManager.loadConfigurationFromJson(newJson)
                                    isJsonValid = result.isSuccess
                                    jsonErrorMessage = result.exceptionOrNull()?.message ?: ""
                                }
                            },
                            onSave = {
                                scope.launch {
                                    val result = languageManager.loadConfigurationFromJson(jsonText)
                                    if (result.isSuccess) {
                                        val config = result.getOrNull()!!
                                        val saved = languageManager.saveLanguageConfiguration(selectedLanguage!!, config)
                                        if (saved) {
                                            currentConfig = config
                                            showJsonEditor = false
                                        }
                                    }
                                }
                            },
                            onBack = { showJsonEditor = false }
                        )
                    } else if (selectedLanguage != null) {
                        // Language Details View
                        LanguageDetailsView(
                            language = selectedLanguage!!,
                            configuration = currentConfig,
                            languageManager = languageManager,
                            onBack = { 
                                selectedLanguage = null
                                currentConfig = null
                            },
                            onEditJson = {
                                scope.launch {
                                    val configJson = languageManager.exportConfigurationToJson(selectedLanguage!!)
                                    if (configJson != null) {
                                        jsonText = configJson
                                        showJsonEditor = true
                                    } else {
                                        // Create template if no config exists
                                        jsonText = languageManager.getExampleTemplate(selectedLanguage!!)
                                        showJsonEditor = true
                                    }
                                }
                            }
                        )
                    } else {
                        // Language List View
                        LanguageListView(
                            onLanguageSelected = { language -> 
                                selectedLanguage = language
                                scope.launch {
                                    currentConfig = languageManager.getLanguageConfiguration(language)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageListView(
    onLanguageSelected: (EditorLanguage) -> Unit
) {
    Column {
        Text(
            text = "LetuZ supports 14+ programming languages with professional syntax highlighting:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EditorLanguage.values().toList()) { language ->
                LanguageCard(
                    language = language,
                    onClick = { onLanguageSelected(language) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enhanced Language System",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Professional VS Code-inspired syntax highlighting\n" +
                          "• Optimized performance for mobile devices\n" +
                          "• Automatic language detection by file extension\n" +
                          "• Built-in support for Kotlin, Java, and 12+ other languages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageCard(
    language: EditorLanguage,
    onClick: () -> Unit
) {
    val languageInfo = getLanguageInfo(language)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = languageInfo.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = languageInfo.extensions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Highlighting level indicator
                val highlightingColor = when (languageInfo.highlightingLevel) {
                    "Full" -> MaterialTheme.colorScheme.primary
                    "Enhanced" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }
                
                Surface(
                    color = highlightingColor.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = languageInfo.highlightingLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = highlightingColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LanguageDetailsView(
    language: EditorLanguage,
    configuration: LanguageConfiguration?,
    languageManager: EnhancedLanguageManager,
    onBack: () -> Unit,
    onEditJson: () -> Unit
) {
    val languageInfo = getLanguageInfo(language)
    
    Column {
        // Header with back button and edit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = languageInfo.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Edit JSON Button
            OutlinedButton(onClick = onEditJson) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit JSON",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit JSON")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageDetailCard(
                title = "File Extensions",
                content = languageInfo.extensions,
                icon = Icons.Default.Description
            )
            
            LanguageDetailCard(
                title = "Syntax Highlighting",
                content = buildString {
                    appendLine("Level: ${languageInfo.highlightingLevel}")
                    appendLine("Engine: ${languageInfo.engine}")
                    append("Features: ${languageInfo.features}")
                },
                icon = Icons.Default.Palette
            )
            
            LanguageDetailCard(
                title = "Language Features",
                content = languageInfo.description,
                icon = Icons.Default.Info
            )
            
            if (language == EditorLanguage.KOTLIN || language == EditorLanguage.JAVA) {
                LanguageDetailCard(
                    title = "Compilation Support",
                    content = "✓ Desktop compilation via ADB\n✓ Real-time error checking\n✓ Program execution\n✓ Output capture",
                    icon = Icons.Default.Build,
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
private fun LanguageDetailCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHighlighted) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content.trim(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = if (content.contains("✓") || content.contains("•")) 
                    FontFamily.Default 
                else 
                    FontFamily.Monospace,
                color = if (isHighlighted) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JsonEditorView(
    language: EditorLanguage,
    jsonText: String,
    isValid: Boolean,
    errorMessage: String,
    onJsonChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Edit ${language.name} Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Button(
                onClick = onSave,
                enabled = isValid && jsonText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // JSON Editor
        OutlinedTextField(
            value = jsonText,
            onValueChange = onJsonChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("JSON Configuration") },
            isError = !isValid,
            supportingText = if (!isValid) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Help text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "💡 JSON Configuration Help",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• keywords: Define syntax highlighting keywords\n" +
                          "• colors: Set custom colors (hex format: #RRGGBB)\n" +
                          "• patterns: Configure regex patterns for syntax elements\n" +
                          "• features: Control editor behavior (indentation, etc.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Data class to hold language information
private data class LanguageInfo(
    val displayName: String,
    val extensions: String,
    val highlightingLevel: String,
    val engine: String,
    val features: String,
    val description: String
)

// Function to get language information
private fun getLanguageInfo(language: EditorLanguage): LanguageInfo {
    return when (language) {
        EditorLanguage.KOTLIN -> LanguageInfo(
            displayName = "Kotlin",
            extensions = ".kt, .kts",
            highlightingLevel = "Full",
            engine = "Sora Editor JavaLanguage",
            features = "Keywords, strings, comments, numbers, operators",
            description = "Modern programming language for Android development with full syntax highlighting, bracket matching, and compilation support."
        )
        EditorLanguage.JAVA -> LanguageInfo(
            displayName = "Java",
            extensions = ".java",
            highlightingLevel = "Full",
            engine = "Sora Editor JavaLanguage",
            features = "Keywords, strings, comments, numbers, operators",
            description = "Object-oriented programming language with full syntax highlighting, bracket matching, and compilation support."
        )
        EditorLanguage.PYTHON -> LanguageInfo(
            displayName = "Python",
            extensions = ".py, .pyw",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Basic highlighting, indentation-aware",
            description = "High-level programming language with enhanced editor support for Python-specific features."
        )
        EditorLanguage.JAVASCRIPT -> LanguageInfo(
            displayName = "JavaScript",
            extensions = ".js, .mjs, .jsx",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Basic highlighting, bracket matching",
            description = "Dynamic programming language for web development with enhanced editor support."
        )
        EditorLanguage.TYPESCRIPT -> LanguageInfo(
            displayName = "TypeScript",
            extensions = ".ts, .tsx",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Basic highlighting, type-aware",
            description = "Typed superset of JavaScript with enhanced editor support for type annotations."
        )
        EditorLanguage.CSHARP -> LanguageInfo(
            displayName = "C#",
            extensions = ".cs",
            highlightingLevel = "Full",
            engine = "Sora Editor JavaLanguage",
            features = "Keywords, strings, comments, numbers",
            description = "Object-oriented programming language similar to Java with full syntax highlighting support."
        )
        EditorLanguage.CPP -> LanguageInfo(
            displayName = "C++",
            extensions = ".cpp, .cxx, .cc, .c, .h, .hpp",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Basic highlighting, preprocessor support",
            description = "Systems programming language with enhanced editor support for C++ specific syntax."
        )
        EditorLanguage.HTML -> LanguageInfo(
            displayName = "HTML",
            extensions = ".html, .htm",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Tag highlighting, attribute support",
            description = "Markup language for web pages with enhanced support for HTML5 features."
        )
        EditorLanguage.CSS -> LanguageInfo(
            displayName = "CSS",
            extensions = ".css",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Selector highlighting, property support",
            description = "Style sheet language for web pages with enhanced support for CSS3 features."
        )
        EditorLanguage.JSON -> LanguageInfo(
            displayName = "JSON",
            extensions = ".json",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Structure highlighting, validation",
            description = "Data interchange format with enhanced support for JSON syntax validation."
        )
        EditorLanguage.XML -> LanguageInfo(
            displayName = "XML",
            extensions = ".xml",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Tag highlighting, namespace support",
            description = "Markup language for structured data with enhanced support for XML features."
        )
        EditorLanguage.YAML -> LanguageInfo(
            displayName = "YAML",
            extensions = ".yml, .yaml",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Indentation-based, key-value highlighting",
            description = "Human-readable data serialization standard with enhanced indentation support."
        )
        EditorLanguage.MARKDOWN -> LanguageInfo(
            displayName = "Markdown",
            extensions = ".md, .markdown",
            highlightingLevel = "Enhanced",
            engine = "Enhanced EmptyLanguage",
            features = "Formatting highlighting, link support",
            description = "Lightweight markup language with enhanced support for Markdown syntax."
        )
        EditorLanguage.PLAIN_TEXT -> LanguageInfo(
            displayName = "Plain Text",
            extensions = ".txt",
            highlightingLevel = "Basic",
            engine = "EmptyLanguage",
            features = "No highlighting, basic editing",
            description = "Simple text files with no syntax highlighting, suitable for notes and documentation."
        )
    }
}