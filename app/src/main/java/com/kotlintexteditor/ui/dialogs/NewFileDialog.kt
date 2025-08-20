package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kotlintexteditor.ui.editor.EditorLanguage

/**
 * Enhanced New File Dialog with language selection, filename input, and location picker
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Create New File",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                // Language Selection
                Text(
                    text = "File Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageOption(
                        language = EditorLanguage.KOTLIN,
                        icon = Icons.Default.Code,
                        title = "Kotlin File",
                        description = "Kotlin source code (.kt)",
                        selected = selectedLanguage == EditorLanguage.KOTLIN,
                        onSelect = { selectedLanguage = EditorLanguage.KOTLIN }
                    )

                    LanguageOption(
                        language = EditorLanguage.JAVA,
                        icon = Icons.Default.Code,
                        title = "Java File",
                        description = "Java source code (.java)",
                        selected = selectedLanguage == EditorLanguage.JAVA,
                        onSelect = { selectedLanguage = EditorLanguage.JAVA }
                    )

                    LanguageOption(
                        language = EditorLanguage.PLAIN_TEXT,
                        icon = Icons.Default.TextSnippet,
                        title = "Text File",
                        description = "Plain text file (.txt)",
                        selected = selectedLanguage == EditorLanguage.PLAIN_TEXT,
                        onSelect = { selectedLanguage = EditorLanguage.PLAIN_TEXT }
                    )
                }

                // File Name Input
                Text(
                    text = "File Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Enter file name") },
                    leadingIcon = {
                        Icon(
                            imageVector = when (selectedLanguage) {
                                EditorLanguage.KOTLIN -> Icons.Default.Code
                                EditorLanguage.JAVA -> Icons.Default.DataObject
                                EditorLanguage.PLAIN_TEXT -> Icons.Default.TextSnippet
                            },
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., MainActivity.kt") }
                )

                // Template Selection
                if (selectedLanguage != EditorLanguage.PLAIN_TEXT) {
                    Text(
                        text = "Template",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val templates = when (selectedLanguage) {
                        EditorLanguage.KOTLIN -> FileTemplate.kotlinTemplates
                        EditorLanguage.JAVA -> FileTemplate.javaTemplates
                        else -> listOf(FileTemplate.EMPTY)
                    }

                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        templates.forEach { template ->
                            TemplateOption(
                                template = template,
                                selected = selectedTemplate == template,
                                onSelect = { selectedTemplate = template }
                            )
                        }
                    }
                }

                Divider()

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    // Create in Memory Button
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                onCreateFile(selectedLanguage, fileName, selectedTemplate)
                            }
                        },
                        enabled = fileName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Create")
                    }

                    // Create and Save Button
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                onCreateFileWithLocation(selectedLanguage, fileName, selectedTemplate)
                            }
                        },
                        enabled = fileName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Save As")
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: EditorLanguage,
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
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
private fun TemplateOption(
    template: FileTemplate,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (template.description.isNotEmpty()) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
