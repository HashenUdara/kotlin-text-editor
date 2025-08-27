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
        val currentExtension = if (fileName.contains('.')) fileName.substringAfterLast('.') else ""
        val defaultExtensions = listOf("kt", "java", "py", "js", "ts", "cs", "cpp", "html", "css", "json", "xml", "yml", "md", "txt")
        
        // Only update if filename is empty or has a default extension from our editor
        if (fileName.isEmpty() || fileName.startsWith("untitled.") || currentExtension in defaultExtensions) {
            val baseName = if (fileName.contains('.')) fileName.substringBeforeLast('.') else "untitled"
            val newExtension = FileTemplate.getDefaultExtension(selectedLanguage)
            fileName = "$baseName.$newExtension"
        }
    }
    
    // Auto-detect language from file extension when user changes filename
    val onFileNameChange: (String) -> Unit = { newFileName ->
        fileName = newFileName
        
        // Auto-detect language if the filename has an extension
        if (newFileName.contains('.')) {
            val extension = newFileName.substringAfterLast('.')
            val detectedLanguage = FileTemplate.detectLanguageFromExtension(extension)
            if (detectedLanguage != selectedLanguage) {
                selectedLanguage = detectedLanguage
                // Reset template to empty when language changes automatically
                selectedTemplate = FileTemplate.EMPTY
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
                    onLanguageSelect = { 
                        selectedLanguage = it
                        selectedTemplate = FileTemplate.EMPTY
                    }
                )
                
                FileNameSection(
                    fileName = fileName,
                    selectedLanguage = selectedLanguage,
                    onFileNameChange = onFileNameChange
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
        
        // All supported languages with their configurations
        val languages = listOf(
            LanguageConfig(EditorLanguage.KOTLIN, "Kotlin", ".kt", Icons.Outlined.Code, MaterialTheme.colorScheme.primary),
            LanguageConfig(EditorLanguage.JAVA, "Java", ".java", Icons.Outlined.DataObject, MaterialTheme.colorScheme.tertiary),
            LanguageConfig(EditorLanguage.PYTHON, "Python", ".py", Icons.Outlined.Code, MaterialTheme.colorScheme.secondary),
            LanguageConfig(EditorLanguage.JAVASCRIPT, "JavaScript", ".js", Icons.Outlined.Javascript, MaterialTheme.colorScheme.primary),
            LanguageConfig(EditorLanguage.TYPESCRIPT, "TypeScript", ".ts", Icons.Outlined.Code, MaterialTheme.colorScheme.primary),
            LanguageConfig(EditorLanguage.CSHARP, "C#", ".cs", Icons.Outlined.Code, MaterialTheme.colorScheme.tertiary),
            LanguageConfig(EditorLanguage.CPP, "C++", ".cpp", Icons.Outlined.Code, MaterialTheme.colorScheme.secondary),
            LanguageConfig(EditorLanguage.HTML, "HTML", ".html", Icons.Outlined.Html, MaterialTheme.colorScheme.primary),
            LanguageConfig(EditorLanguage.CSS, "CSS", ".css", Icons.Outlined.Css, MaterialTheme.colorScheme.secondary),
            LanguageConfig(EditorLanguage.JSON, "JSON", ".json", Icons.Outlined.DataObject, MaterialTheme.colorScheme.tertiary),
            LanguageConfig(EditorLanguage.XML, "XML", ".xml", Icons.Outlined.Code, MaterialTheme.colorScheme.primary),
            LanguageConfig(EditorLanguage.YAML, "YAML", ".yml", Icons.Outlined.Description, MaterialTheme.colorScheme.secondary),
            LanguageConfig(EditorLanguage.MARKDOWN, "Markdown", ".md", Icons.Outlined.Description, MaterialTheme.colorScheme.tertiary),
            LanguageConfig(EditorLanguage.PLAIN_TEXT, "Plain Text", ".txt", Icons.Outlined.TextSnippet, MaterialTheme.colorScheme.primary)
        )
        
        // Display languages in a grid (3 columns)
        languages.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { config ->
                    LanguageCard(
                        language = config.language,
                        icon = config.icon,
                        title = config.title,
                        description = config.extension,
                        color = config.color,
                        selected = selectedLanguage == config.language,
                        modifier = Modifier.weight(1f),
                        onSelect = { onLanguageSelect(config.language) }
                    )
                }
                
                // Add empty spacers if the row has fewer than 3 items
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Data class to hold language configuration
 */
private data class LanguageConfig(
    val language: EditorLanguage,
    val title: String,
    val extension: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

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
        
        val templates = FileTemplate.getTemplatesForLanguage(selectedLanguage)
        
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
    ),
    
    // Python Templates
    PYTHON_SCRIPT(
        "Python Script",
        "Basic Python script with main",
        """#!/usr/bin/env python3

def main():
    print("Hello, World!")

if __name__ == "__main__":
    main()"""
    ),
    
    PYTHON_CLASS(
        "Python Class",
        "Basic Python class template",
        """class ${"{fileName}"}:
    \"\"\"Class description\"\"\"
    
    def __init__(self):
        pass
    
    def __str__(self):
        return f"${"{fileName}"}()"
"""
    ),
    
    PYTHON_FUNCTION(
        "Python Function",
        "Basic Python function template",
        """def ${"{fileName}"}():
    \"\"\"Function description\"\"\"
    pass
"""
    ),
    
    PYTHON_FLASK_APP(
        "Flask App",
        "Basic Flask web application",
        """from flask import Flask

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

if __name__ == '__main__':
    app.run(debug=True)"""
    ),
    
    // JavaScript Templates
    JAVASCRIPT_FUNCTION(
        "JavaScript Function",
        "Basic JavaScript function",
        """function ${"{fileName}"}() {
    console.log("Hello, World!");
}

${"{fileName}"}();"""
    ),
    
    JAVASCRIPT_MODULE(
        "ES6 Module",
        "ES6 module with exports",
        """// ${"{fileName}"}.js

export function ${"{fileName}"}() {
    console.log("Hello from module!");
}

export default ${"{fileName}"};"""
    ),
    
    JAVASCRIPT_CLASS(
        "JavaScript Class",
        "ES6 class template",
        """class ${"{fileName}"} {
    constructor() {
        // Constructor
    }
    
    method() {
        console.log("Method called");
    }
}

export default ${"{fileName}"};"""
    ),
    
    JAVASCRIPT_REACT_COMPONENT(
        "React Component",
        "React functional component",
        """import React from 'react';

function ${"{fileName}"}() {
    return (
        <div>
            <h1>Hello from ${"{fileName}"}!</h1>
        </div>
    );
}

export default ${"{fileName}"};"""
    ),
    
    // TypeScript Templates
    TYPESCRIPT_INTERFACE(
        "TypeScript Interface",
        "TypeScript interface definition",
        """export interface ${"{fileName}"} {
    id: number;
    name: string;
}"""
    ),
    
    TYPESCRIPT_CLASS(
        "TypeScript Class",
        "TypeScript class with types",
        """export class ${"{fileName}"} {
    private id: number;
    private name: string;
    
    constructor(id: number, name: string) {
        this.id = id;
        this.name = name;
    }
    
    public getId(): number {
        return this.id;
    }
    
    public getName(): string {
        return this.name;
    }
}"""
    ),
    
    TYPESCRIPT_FUNCTION(
        "TypeScript Function",
        "TypeScript function with types",
        """export function ${"{fileName}"}(param: string): string {
    return `Hello, ${"$"}{param}!`;
}"""
    ),
    
    // C# Templates
    CSHARP_CLASS(
        "C# Class",
        "Basic C# class template",
        """using System;

namespace MyNamespace
{
    public class ${"{fileName}"}
    {
        public ${"{fileName}"}()
        {
            // Constructor
        }
    }
}"""
    ),
    
    CSHARP_PROGRAM(
        "C# Program",
        "C# console application",
        """using System;

namespace MyApplication
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Hello, World!");
        }
    }
}"""
    ),
    
    CSHARP_INTERFACE(
        "C# Interface",
        "C# interface definition",
        """using System;

namespace MyNamespace
{
    public interface I${"{fileName}"}
    {
        void DoSomething();
    }
}"""
    ),
    
    // C++ Templates
    CPP_MAIN(
        "C++ Main",
        "C++ program with main function",
        """#include <iostream>

int main() {
    std::cout << "Hello, World!" << std::endl;
    return 0;
}"""
    ),
    
    CPP_CLASS(
        "C++ Class",
        "C++ class with header",
        """#ifndef ${"{fileName}".uppercase()}_H
#define ${"{fileName}".uppercase()}_H

class ${"{fileName}"} {
private:
    // Private members

public:
    ${"{fileName}"}();
    ~${"{fileName}"}();
    
    // Public methods
};

#endif // ${"{fileName}".uppercase()}_H"""
    ),
    
    CPP_FUNCTION(
        "C++ Function",
        "C++ function template",
        """#include <iostream>

void ${"{fileName}"}() {
    std::cout << "Function called" << std::endl;
}

int main() {
    ${"{fileName}"}();
    return 0;
}"""
    ),
    
    // HTML Templates
    HTML_BASIC(
        "Basic HTML",
        "Basic HTML5 document",
        """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <h1>Hello, World!</h1>
</body>
</html>"""
    ),
    
    HTML_TEMPLATE(
        "HTML Template",
        "Complete HTML template with CSS",
        """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${"{fileName}"}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
        }
    </style>
</head>
<body>
    <header>
        <h1>Welcome to ${"{fileName}"}</h1>
    </header>
    
    <main>
        <p>Your content here...</p>
    </main>
    
    <script>
        console.log('Page loaded');
    </script>
</body>
</html>"""
    ),
    
    // CSS Templates
    CSS_RESET(
        "CSS Reset",
        "CSS reset stylesheet",
        """/* CSS Reset */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: Arial, sans-serif;
    line-height: 1.6;
    color: #333;
}

h1, h2, h3, h4, h5, h6 {
    margin-bottom: 0.5em;
}

p {
    margin-bottom: 1em;
}"""
    ),
    
    CSS_COMPONENT(
        "CSS Component",
        "CSS component styles",
        """.${"{fileName}"} {
    /* Component styles */
    display: block;
    padding: 1rem;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.${"{fileName}"}__title {
    font-size: 1.2em;
    font-weight: bold;
    margin-bottom: 0.5rem;
}

.${"{fileName}"}__content {
    color: #666;
}

.${"{fileName}"}--active {
    border-color: #007bff;
}"""
    ),
    
    // JSON Templates
    JSON_OBJECT(
        "JSON Object",
        "Basic JSON object",
        """{
    "name": "Example",
    "version": "1.0.0",
    "description": "A sample JSON object",
    "data": {
        "items": [],
        "count": 0
    }
}"""
    ),
    
    JSON_ARRAY(
        "JSON Array",
        "JSON array template",
        """[
    {
        "id": 1,
        "name": "Item 1",
        "active": true
    },
    {
        "id": 2,
        "name": "Item 2",
        "active": false
    }
]"""
    ),
    
    JSON_CONFIG(
        "JSON Config",
        "Configuration file template",
        """{
    "app": {
        "name": "${"{fileName}"}",
        "version": "1.0.0",
        "debug": false
    },
    "database": {
        "host": "localhost",
        "port": 5432,
        "name": "mydb"
    },
    "features": {
        "enableLogging": true,
        "maxConnections": 100
    }
}"""
    ),
    
    // XML Templates
    XML_DOCUMENT(
        "XML Document",
        "Basic XML document",
        """<?xml version="1.0" encoding="UTF-8"?>
<root>
    <item id="1">
        <name>Example Item</name>
        <description>This is an example XML item</description>
    </item>
</root>"""
    ),
    
    // YAML Templates
    YAML_CONFIG(
        "YAML Config",
        "YAML configuration file",
        """# ${"{fileName}"} configuration
app:
  name: "${"{fileName}"}"
  version: "1.0.0"
  debug: false

database:
  host: localhost
  port: 5432
  name: mydb

features:
  enableLogging: true
  maxConnections: 100"""
    ),
    
    // Markdown Templates
    MARKDOWN_README(
        "README.md",
        "Project README template",
        """# ${"{fileName}"}

## Description
Brief description of your project.

## Installation
```bash
# Installation instructions
```

## Usage
```bash
# Usage examples
```

## Features
- Feature 1
- Feature 2
- Feature 3

## Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License
MIT License"""
    ),
    
    MARKDOWN_DOCUMENT(
        "Markdown Document",
        "Basic markdown document",
        """# ${"{fileName}"}

## Introduction
This is a markdown document.

## Sections

### Subsection 1
Content here...

### Subsection 2
More content...

## Lists

- Item 1
- Item 2
- Item 3

## Code Example
```bash
echo "Hello, World!"
```

## Links
[Example Link](https://example.com)
""");

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
        
        val pythonTemplates = listOf(
            EMPTY,
            PYTHON_SCRIPT,
            PYTHON_CLASS,
            PYTHON_FUNCTION,
            PYTHON_FLASK_APP
        )
        
        val javascriptTemplates = listOf(
            EMPTY,
            JAVASCRIPT_FUNCTION,
            JAVASCRIPT_MODULE,
            JAVASCRIPT_CLASS,
            JAVASCRIPT_REACT_COMPONENT
        )
        
        val typescriptTemplates = listOf(
            EMPTY,
            TYPESCRIPT_INTERFACE,
            TYPESCRIPT_CLASS,
            TYPESCRIPT_FUNCTION
        )
        
        val csharpTemplates = listOf(
            EMPTY,
            CSHARP_CLASS,
            CSHARP_PROGRAM,
            CSHARP_INTERFACE
        )
        
        val cppTemplates = listOf(
            EMPTY,
            CPP_MAIN,
            CPP_CLASS,
            CPP_FUNCTION
        )
        
        val htmlTemplates = listOf(
            EMPTY,
            HTML_BASIC,
            HTML_TEMPLATE
        )
        
        val cssTemplates = listOf(
            EMPTY,
            CSS_RESET,
            CSS_COMPONENT
        )
        
        val jsonTemplates = listOf(
            EMPTY,
            JSON_OBJECT,
            JSON_ARRAY,
            JSON_CONFIG
        )
        
        val xmlTemplates = listOf(
            EMPTY,
            XML_DOCUMENT
        )
        
        val yamlTemplates = listOf(
            EMPTY,
            YAML_CONFIG
        )
        
        val markdownTemplates = listOf(
            EMPTY,
            MARKDOWN_README,
            MARKDOWN_DOCUMENT
        )
        
        /**
         * Get templates for a specific language
         */
        fun getTemplatesForLanguage(language: EditorLanguage): List<FileTemplate> {
            return when (language) {
                EditorLanguage.KOTLIN -> kotlinTemplates
                EditorLanguage.JAVA -> javaTemplates
                EditorLanguage.PYTHON -> pythonTemplates
                EditorLanguage.JAVASCRIPT -> javascriptTemplates
                EditorLanguage.TYPESCRIPT -> typescriptTemplates
                EditorLanguage.CSHARP -> csharpTemplates
                EditorLanguage.CPP -> cppTemplates
                EditorLanguage.HTML -> htmlTemplates
                EditorLanguage.CSS -> cssTemplates
                EditorLanguage.JSON -> jsonTemplates
                EditorLanguage.XML -> xmlTemplates
                EditorLanguage.YAML -> yamlTemplates
                EditorLanguage.MARKDOWN -> markdownTemplates
                EditorLanguage.PLAIN_TEXT -> listOf(EMPTY)
            }
        }
        
        /**
         * Get default file extension for a language
         */
        fun getDefaultExtension(language: EditorLanguage): String {
            return when (language) {
                EditorLanguage.KOTLIN -> "kt"
                EditorLanguage.JAVA -> "java"
                EditorLanguage.PYTHON -> "py"
                EditorLanguage.JAVASCRIPT -> "js"
                EditorLanguage.TYPESCRIPT -> "ts"
                EditorLanguage.CSHARP -> "cs"
                EditorLanguage.CPP -> "cpp"
                EditorLanguage.HTML -> "html"
                EditorLanguage.CSS -> "css"
                EditorLanguage.JSON -> "json"
                EditorLanguage.XML -> "xml"
                EditorLanguage.YAML -> "yml"
                EditorLanguage.MARKDOWN -> "md"
                EditorLanguage.PLAIN_TEXT -> "txt"
            }
        }
        
        /**
         * Detect language from file extension
         */
        fun detectLanguageFromExtension(extension: String): EditorLanguage {
            return when (extension.lowercase()) {
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
    }
    
    /**
     * Get template content with filename substitution
     */
    fun getContent(fileName: String): String {
        val className = fileName.substringBeforeLast('.').replaceFirstChar { it.uppercase() }
        return content.replace("{fileName}", className)
    }
}
