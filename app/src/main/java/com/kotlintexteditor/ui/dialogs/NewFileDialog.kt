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
                EditorLanguage.PYTHON -> "untitled.py"
                EditorLanguage.JAVASCRIPT -> "untitled.js"
                EditorLanguage.TYPESCRIPT -> "untitled.ts"
                EditorLanguage.CSHARP -> "untitled.cs"
                EditorLanguage.CPP -> "untitled.cpp"
                EditorLanguage.HTML -> "untitled.html"
                EditorLanguage.CSS -> "untitled.css"
                EditorLanguage.JSON -> "untitled.json"
                EditorLanguage.XML -> "untitled.xml"
                EditorLanguage.YAML -> "untitled.yml"
                EditorLanguage.MARKDOWN -> "untitled.md"
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
                .fillMaxWidth(0.92f)
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.NoteAdd,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Create New File",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choose file type, name, and template",
            style = MaterialTheme.typography.bodySmall,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(
            title = "File Name",
            icon = Icons.Outlined.DriveFileRenameOutline
        )
        
        OutlinedTextField(
            value = fileName,
            onValueChange = onFileNameChange,
            label = { Text("Enter file name", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    imageVector = when (selectedLanguage) {
                        EditorLanguage.KOTLIN -> Icons.Outlined.Code
                        EditorLanguage.JAVA -> Icons.Outlined.DataObject
                        EditorLanguage.PYTHON -> Icons.Outlined.Code
                        EditorLanguage.JAVASCRIPT -> Icons.Outlined.Javascript
                        EditorLanguage.TYPESCRIPT -> Icons.Outlined.Code
                        EditorLanguage.CSHARP -> Icons.Outlined.Code
                        EditorLanguage.CPP -> Icons.Outlined.Code
                        EditorLanguage.HTML -> Icons.Outlined.Html
                        EditorLanguage.CSS -> Icons.Outlined.Css
                        EditorLanguage.JSON -> Icons.Outlined.DataObject
                        EditorLanguage.XML -> Icons.Outlined.Code
                        EditorLanguage.YAML -> Icons.Outlined.Description
                        EditorLanguage.MARKDOWN -> Icons.Outlined.Description
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
                        EditorLanguage.PYTHON -> "script.py"
                        EditorLanguage.JAVASCRIPT -> "script.js"
                        EditorLanguage.TYPESCRIPT -> "script.ts"
                        EditorLanguage.CSHARP -> "Program.cs"
                        EditorLanguage.CPP -> "main.cpp"
                        EditorLanguage.HTML -> "index.html"
                        EditorLanguage.CSS -> "styles.css"
                        EditorLanguage.JSON -> "data.json"
                        EditorLanguage.XML -> "document.xml"
                        EditorLanguage.YAML -> "config.yml"
                        EditorLanguage.MARKDOWN -> "README.md"
                        EditorLanguage.PLAIN_TEXT -> "document.txt"
                    },
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            textStyle = MaterialTheme.typography.bodyMedium
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(
            title = "Template",
            icon = Icons.Outlined.Inventory
        )
        
        val templates = when (selectedLanguage) {
            EditorLanguage.KOTLIN -> FileTemplate.kotlinTemplates
            EditorLanguage.JAVA -> FileTemplate.javaTemplates
            EditorLanguage.PYTHON -> listOf(FileTemplate.EMPTY)
            EditorLanguage.JAVASCRIPT -> listOf(FileTemplate.EMPTY)
            EditorLanguage.TYPESCRIPT -> listOf(FileTemplate.EMPTY)
            EditorLanguage.CSHARP -> listOf(FileTemplate.EMPTY)
            EditorLanguage.CPP -> listOf(FileTemplate.EMPTY)
            EditorLanguage.HTML -> listOf(FileTemplate.EMPTY)
            EditorLanguage.CSS -> listOf(FileTemplate.EMPTY)
            EditorLanguage.JSON -> listOf(FileTemplate.EMPTY)
            EditorLanguage.XML -> listOf(FileTemplate.EMPTY)
            EditorLanguage.YAML -> listOf(FileTemplate.EMPTY)
            EditorLanguage.MARKDOWN -> listOf(FileTemplate.EMPTY)
            EditorLanguage.PLAIN_TEXT -> listOf(FileTemplate.EMPTY)
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            thickness = 0.5.dp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel Button
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
            
            // Create Button
            Button(
                onClick = onCreateFile,
                enabled = fileName.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Create,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Create",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // Save As Button (Full Width)
        Button(
            onClick = onCreateFileWithLocation,
            enabled = fileName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.SaveAs,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Create & Save As...",
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
private fun LazyTemplateGrid(
    templates: List<FileTemplate>,
    selectedTemplate: FileTemplate,
    onTemplateSelect: (FileTemplate) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
