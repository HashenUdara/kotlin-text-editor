package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlintexteditor.ui.editor.EditorLanguage

/**
 * Modern File Browser Dialog for creating and opening files
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCreateFile: (language: EditorLanguage, fileName: String, template: FileTemplate) -> Unit,
    onOpenFile: () -> Unit
) {
    if (!isVisible) return

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Create New", "Open Existing")

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
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                DialogHeader()
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.Outlined.Create else Icons.Outlined.FolderOpen,
                                    contentDescription = title,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                
                // Content based on selected tab
                when (selectedTab) {
                    0 -> CreateFileSection(onCreateFile = onCreateFile)
                    1 -> OpenFileSection(onOpenFile = onOpenFile)
                }
                
                // Action Buttons
                ActionButtonsSection(
                    onDismiss = onDismiss,
                    onPrimaryAction = {
                        when (selectedTab) {
                            0 -> { /* Create action handled in section */ }
                            1 -> onOpenFile()
                        }
                    },
                    primaryActionText = if (selectedTab == 0) "Create" else "Open"
                )
            }
        }
    }
}

@Composable
private fun DialogHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Storage,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "File Browser",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create new files or open existing ones",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreateFileSection(
    onCreateFile: (language: EditorLanguage, fileName: String, template: FileTemplate) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(EditorLanguage.KOTLIN) }
    var fileName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(FileTemplate.EMPTY) }

    // Update filename when language changes
    LaunchedEffect(selectedLanguage) {
        if (fileName.isEmpty() || fileName == "untitled.kt" || fileName == "untitled.java" || fileName == "untitled.txt") {
            fileName = when (selectedLanguage) {
                EditorLanguage.KOTLIN -> "untitled.kt"
                EditorLanguage.JAVA -> "untitled.java"
                EditorLanguage.PLAIN_TEXT -> "untitled.txt"
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Language Selection
        SectionTitle(
            title = "File Type",
            icon = Icons.Outlined.Category
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageCard(
                language = EditorLanguage.KOTLIN,
                icon = Icons.Outlined.Code,
                title = "Kotlin",
                description = ".kt",
                color = MaterialTheme.colorScheme.primary,
                selected = selectedLanguage == EditorLanguage.KOTLIN,
                modifier = Modifier.weight(1f),
                onSelect = { selectedLanguage = EditorLanguage.KOTLIN }
            )
            
            LanguageCard(
                language = EditorLanguage.JAVA,
                icon = Icons.Outlined.DataObject,
                title = "Java",
                description = ".java",
                color = MaterialTheme.colorScheme.tertiary,
                selected = selectedLanguage == EditorLanguage.JAVA,
                modifier = Modifier.weight(1f),
                onSelect = { selectedLanguage = EditorLanguage.JAVA }
            )
            
            LanguageCard(
                language = EditorLanguage.PLAIN_TEXT,
                icon = Icons.Outlined.TextSnippet,
                title = "Text",
                description = ".txt",
                color = MaterialTheme.colorScheme.secondary,
                selected = selectedLanguage == EditorLanguage.PLAIN_TEXT,
                modifier = Modifier.weight(1f),
                onSelect = { selectedLanguage = EditorLanguage.PLAIN_TEXT }
            )
        }
        
        // File Name Input
        SectionTitle(
            title = "File Name",
            icon = Icons.Outlined.DriveFileRenameOutline
        )
        
        OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("Enter file name") },
            leadingIcon = {
                Icon(
                    imageVector = when (selectedLanguage) {
                        EditorLanguage.KOTLIN -> Icons.Outlined.Code
                        EditorLanguage.JAVA -> Icons.Outlined.DataObject
                        EditorLanguage.PLAIN_TEXT -> Icons.Outlined.TextSnippet
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    when (selectedLanguage) {
                        EditorLanguage.KOTLIN -> "MainActivity.kt"
                        EditorLanguage.JAVA -> "MainActivity.java"
                        EditorLanguage.PLAIN_TEXT -> "document.txt"
                    }
                ) 
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        
        // Template Selection
        if (selectedLanguage != EditorLanguage.PLAIN_TEXT) {
            SectionTitle(
                title = "Template",
                icon = Icons.Outlined.Inventory
            )
            
            val templates = when (selectedLanguage) {
                EditorLanguage.KOTLIN -> FileTemplate.kotlinTemplates
                EditorLanguage.JAVA -> FileTemplate.javaTemplates
                else -> listOf(FileTemplate.EMPTY)
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        selected = selectedTemplate == template,
                        onSelect = { selectedTemplate = template }
                    )
                }
            }
        }
        
        // Create Button
        Button(
            onClick = {
                if (fileName.isNotBlank()) {
                    onCreateFile(selectedLanguage, fileName, selectedTemplate)
                }
            },
            enabled = fileName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Create,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Create File",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun OpenFileSection(
    onOpenFile: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = "Open Existing File",
            icon = Icons.Outlined.FolderOpen
        )
        
        // File type info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Supported File Types",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FileTypeChip("Kotlin (.kt)", Icons.Outlined.Code, MaterialTheme.colorScheme.primary)
                    FileTypeChip("Java (.java)", Icons.Outlined.DataObject, MaterialTheme.colorScheme.tertiary)
                    FileTypeChip("Text (.txt)", Icons.Outlined.TextSnippet, MaterialTheme.colorScheme.secondary)
                }
                
                Text(
                    text = "Click the button below to browse and open files from your device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Open Button
        Button(
            onClick = onOpenFile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Browse Files",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun FileTypeChip(
    text: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    primaryActionText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelMedium
            )
        }
        
        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = primaryActionText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LanguageCard(
    language: EditorLanguage,
    icon: ImageVector,
    title: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val backgroundColor = if (selected) {
        color.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val borderColor = if (selected) {
        color
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 0.8.dp,
            color = borderColor
        ),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: FileTemplate,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (selected) {
            BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = template.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (template.description.isNotEmpty()) {
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}
