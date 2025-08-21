package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlintexteditor.syntax.ConfigurableEditorManager
import com.kotlintexteditor.syntax.LanguageConfiguration
import com.kotlintexteditor.syntax.SupportedLanguage
import kotlinx.coroutines.launch

/**
 * Dialog for managing language configurations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigurationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val context = LocalContext.current
        val configurableManager = remember { ConfigurableEditorManager.getInstance(context) }
        val scope = rememberCoroutineScope()
        
        var selectedLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }
        var selectedConfig by remember { mutableStateOf<LanguageConfiguration?>(null) }
        var showConfigDetails by remember { mutableStateOf(false) }
        var configJson by remember { mutableStateOf("") }
        var showJsonEditor by remember { mutableStateOf(false) }
        
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
                            text = "Language Configurations",
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
                            initialJson = configJson,
                            configurableManager = configurableManager,
                            onBack = { 
                                showJsonEditor = false
                                showConfigDetails = true
                            },
                            onSave = { language, json ->
                                scope.launch {
                                    val result = configurableManager.loadConfigurationFromJson(json)
                                    if (result.isSuccess) {
                                        configurableManager.updateLanguageConfiguration(language, result.getOrNull()!!)
                                        selectedConfig = result.getOrNull()
                                        showJsonEditor = false
                                        showConfigDetails = true
                                    }
                                }
                            }
                        )
                    } else if (showConfigDetails && selectedLanguage != null && selectedConfig != null) {
                        // Configuration Details View
                        ConfigurationDetailsView(
                            language = selectedLanguage!!,
                            configuration = selectedConfig!!,
                            onBack = { 
                                showConfigDetails = false
                                selectedLanguage = null
                                selectedConfig = null
                            },
                            onEditJson = {
                                configJson = configurableManager.exportConfigurationToJson(selectedLanguage!!) ?: ""
                                showJsonEditor = true
                                showConfigDetails = false
                            }
                        )
                    } else {
                        // Language List View
                        LanguageListView(
                            availableLanguages = configurableManager.getAvailableLanguages(),
                            onLanguageSelected = { language ->
                                selectedLanguage = language
                                selectedConfig = configurableManager.getLanguageConfiguration(language)
                                showConfigDetails = true
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
    availableLanguages: List<SupportedLanguage>,
    onLanguageSelected: (SupportedLanguage) -> Unit
) {
    Text(
        text = "Select a language to view or modify its configuration:",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableLanguages) { language ->
            LanguageCard(
                language = language,
                onClick = { onLanguageSelected(language) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    onClick: () -> Unit
) {
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
                    text = language.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${language.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View configuration",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfigurationDetailsView(
    language: SupportedLanguage,
    configuration: LanguageConfiguration,
    onBack: () -> Unit,
    onEditJson: () -> Unit
) {
    Column {
        // Header with back button
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
                    text = configuration.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
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
        
        // Configuration details
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ConfigurationSection(
                    title = "File Extensions",
                    content = configuration.fileExtensions.joinToString(", ") { ".$it" }
                )
            }
            
            item {
                ConfigurationSection(
                    title = "Keywords",
                    content = buildString {
                        appendLine("Primary: ${configuration.keywords.primary.size} keywords")
                        appendLine("Types: ${configuration.keywords.types.size} types")
                        appendLine("Functions: ${configuration.keywords.functions.size} functions")
                        appendLine("Literals: ${configuration.keywords.literals.size} literals")
                    }
                )
            }
            
            item {
                ConfigurationSection(
                    title = "Comments",
                    content = buildString {
                        configuration.patterns.singleLineComment?.let {
                            appendLine("Single line: $it")
                        }
                        if (configuration.patterns.multiLineCommentStart != null && 
                            configuration.patterns.multiLineCommentEnd != null) {
                            appendLine("Multi line: ${configuration.patterns.multiLineCommentStart} ... ${configuration.patterns.multiLineCommentEnd}")
                        }
                    }
                )
            }
            
            item {
                ConfigurationSection(
                    title = "Features",
                    content = buildString {
                        appendLine("Indent size: ${configuration.features.indentSize}")
                        appendLine("Uses tabs: ${configuration.features.usesTabs}")
                        appendLine("Auto indent: ${configuration.features.autoIndent}")
                        appendLine("Case sensitive: ${configuration.features.caseSensitive}")
                        appendLine("Bracket matching: ${configuration.features.bracketMatching}")
                    }
                )
            }
        }
    }
}

@Composable
private fun ConfigurationSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            SelectionContainer {
                Text(
                    text = content.trim(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JsonEditorView(
    language: SupportedLanguage,
    initialJson: String,
    configurableManager: ConfigurableEditorManager,
    onBack: () -> Unit,
    onSave: (SupportedLanguage, String) -> Unit
) {
    var jsonText by remember { mutableStateOf(initialJson) }
    var isValid by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
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
                    text = "Edit ${language.displayName} Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Button(
                onClick = { onSave(language, jsonText) },
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
            onValueChange = { newValue ->
                jsonText = newValue
                // Validate JSON
                scope.launch {
                    val result = configurableManager.loadConfigurationFromJson(newValue)
                    isValid = result.isSuccess
                    errorMessage = result.exceptionOrNull()?.message ?: ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("JSON Configuration") },
            isError = !isValid,
            supportingText = if (!isValid) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tip: Modify the JSON configuration to customize syntax highlighting rules, keywords, and color schemes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
