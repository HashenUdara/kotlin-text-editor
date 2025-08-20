package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
 * Modern Material Design 3 New File Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCreateFile: (language: EditorLanguage, fileName: String, template: FileTemplate) -> Unit,
    onCreateFileWithLocation: (language: EditorLanguage, fileName: String, template: FileTemplate) -> Unit
) {
    if (!isVisible) return

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
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Section
                DialogHeader()
                
                // Content Sections
                LanguageSelectionSection(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelect = { selectedLanguage = it }
                )
                
                FileNameSection(
                    fileName = fileName,
                    selectedLanguage = selectedLanguage,
                    onFileNameChange = { fileName = it }
                )
                
                if (selectedLanguage != EditorLanguage.PLAIN_TEXT) {
                    TemplateSelectionSection(
                        selectedLanguage = selectedLanguage,
                        selectedTemplate = selectedTemplate,
                        onTemplateSelect = { selectedTemplate = it }
                    )
                }
                
                // Action Buttons
                ActionButtonsSection(
                    fileName = fileName,
                    onDismiss = onDismiss,
                    onCreateFile = { onCreateFile(selectedLanguage, fileName, selectedTemplate) },
                    onCreateFileWithLocation = { onCreateFileWithLocation(selectedLanguage, fileName, selectedTemplate) }
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
            imageVector = Icons.Outlined.NoteAdd,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Create New File",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choose file type, name, and template",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LanguageSelectionSection(
    selectedLanguage: EditorLanguage,
    onLanguageSelect: (EditorLanguage) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = "File Type",
            icon = Icons.Outlined.Category
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageCard(
                language = EditorLanguage.KOTLIN,
                icon = Icons.Outlined.Code,
                title = "Kotlin",
                description = ".kt",
                color = MaterialTheme.colorScheme.primary,
                selected = selectedLanguage == EditorLanguage.KOTLIN,
                modifier = Modifier.weight(1f),
                onSelect = { onLanguageSelect(EditorLanguage.KOTLIN) }
            )
            
            LanguageCard(
                language = EditorLanguage.JAVA,
                icon = Icons.Outlined.DataObject,
                title = "Java",
                description = ".java",
                color = MaterialTheme.colorScheme.tertiary,
                selected = selectedLanguage == EditorLanguage.JAVA,
                modifier = Modifier.weight(1f),
                onSelect = { onLanguageSelect(EditorLanguage.JAVA) }
            )
            
            LanguageCard(
                language = EditorLanguage.PLAIN_TEXT,
                icon = Icons.Outlined.TextSnippet,
                title = "Text",
                description = ".txt",
                color = MaterialTheme.colorScheme.secondary,
                selected = selectedLanguage == EditorLanguage.PLAIN_TEXT,
                modifier = Modifier.weight(1f),
                onSelect = { onLanguageSelect(EditorLanguage.PLAIN_TEXT) }
            )
        }
    }
}

@Composable
private fun FileNameSection(
    fileName: String,
    selectedLanguage: EditorLanguage,
    onFileNameChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = "File Name",
            icon = Icons.Outlined.DriveFileRenameOutline
        )
        
        OutlinedTextField(
            value = fileName,
            onValueChange = onFileNameChange,
            label = { Text("Enter file name") },
            leadingIcon = {
                Icon(
                    imageVector = when (selectedLanguage) {
                        EditorLanguage.KOTLIN -> Icons.Outlined.Code
                        EditorLanguage.JAVA -> Icons.Outlined.DataObject
                        EditorLanguage.PLAIN_TEXT -> Icons.Outlined.TextSnippet
                    },
                    contentDescription = null,
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
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun TemplateSelectionSection(
    selectedLanguage: EditorLanguage,
    selectedTemplate: FileTemplate,
    onTemplateSelect: (FileTemplate) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = "Template",
            icon = Icons.Outlined.Inventory
        )
        
        val templates = when (selectedLanguage) {
            EditorLanguage.KOTLIN -> FileTemplate.kotlinTemplates
            EditorLanguage.JAVA -> FileTemplate.javaTemplates
            else -> listOf(FileTemplate.EMPTY)
        }
        
        LazyTemplateGrid(
            templates = templates,
            selectedTemplate = selectedTemplate,
            onTemplateSelect = onTemplateSelect
        )
    }
}

@Composable
private fun ActionButtonsSection(
    fileName: String,
    onDismiss: () -> Unit,
    onCreateFile: () -> Unit,
    onCreateFileWithLocation: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            // Create Button
            Button(
                onClick = onCreateFile,
                enabled = fileName.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
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
                    text = "Create",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        // Save As Button (Full Width)
        Button(
            onClick = onCreateFileWithLocation,
            enabled = fileName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.SaveAs,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Create & Save As...",
                style = MaterialTheme.typography.labelLarge
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LazyTemplateGrid(
    templates: List<FileTemplate>,
    selectedTemplate: FileTemplate,
    onTemplateSelect: (FileTemplate) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        templates.forEach { template ->
            TemplateCard(
                template = template,
                selected = selectedTemplate == template,
                onSelect = { onTemplateSelect(template) }
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = template.displayName,
                    style = MaterialTheme.typography.bodyLarge,
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



/**
 * File templates for different languages
 */
enum class FileTemplate(
    val displayName: String,
    val description: String,
    val content: String
) {
    EMPTY(
        "Empty File",
        "Start with a blank file",
        ""
    ),
    
    KOTLIN_CLASS(
        "Kotlin Class",
        "Basic Kotlin class template",
        """class ${"{fileName}"} {
    
}"""
    ),
    
    KOTLIN_DATA_CLASS(
        "Kotlin Data Class",
        "Data class with properties",
        """data class ${"{fileName}"}(
    val id: Int,
    val name: String
)"""
    ),
    
    KOTLIN_MAIN_FUNCTION(
        "Main Function",
        "Kotlin main function template",
        """fun main() {
    println("Hello, World!")
}"""
    ),
    
    KOTLIN_ANDROID_ACTIVITY(
        "Android Activity",
        "Basic Android Activity class",
        """import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ${"{fileName}"} : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO: Add your composable content here
        }
    }
}"""
    ),
    
    JAVA_CLASS(
        "Java Class",
        "Basic Java class template",
        """public class ${"{fileName}"} {
    
}"""
    ),
    
    JAVA_MAIN_CLASS(
        "Main Class",
        "Java class with main method",
        """public class ${"{fileName}"} {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}"""
    ),
    
    JAVA_INTERFACE(
        "Java Interface",
        "Basic Java interface template",
        """public interface ${"{fileName}"} {
    
}"""
    );

    companion object {
        val kotlinTemplates = listOf(
            EMPTY,
            KOTLIN_CLASS,
            KOTLIN_DATA_CLASS,
            KOTLIN_MAIN_FUNCTION,
            KOTLIN_ANDROID_ACTIVITY
        )
        
        val javaTemplates = listOf(
            EMPTY,
            JAVA_CLASS,
            JAVA_MAIN_CLASS,
            JAVA_INTERFACE
        )
    }
    
    /**
     * Get template content with filename substitution
     */
    fun getContent(fileName: String): String {
        val className = fileName.substringBeforeLast('.').replaceFirstChar { it.uppercase() }
        return content.replace("{fileName}", className)
    }
}
