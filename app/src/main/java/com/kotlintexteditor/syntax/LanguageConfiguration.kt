package com.kotlintexteditor.syntax

import kotlinx.serialization.Serializable

/**
 * Configuration for a programming language's syntax highlighting
 */
@Serializable
data class LanguageConfiguration(
    val name: String,
    val fileExtensions: List<String>,
    val keywords: LanguageKeywords,
    val patterns: LanguagePatterns,
    val colors: LanguageColors? = null,
    val features: LanguageFeatures = LanguageFeatures()
)

/**
 * Keywords for different categories
 */
@Serializable
data class LanguageKeywords(
    val primary: List<String> = emptyList(),        // Main keywords (if, else, while, etc.)
    val secondary: List<String> = emptyList(),      // Secondary keywords (public, private, etc.)
    val types: List<String> = emptyList(),          // Type keywords (int, String, etc.)
    val literals: List<String> = emptyList(),       // Literal keywords (true, false, null, etc.)
    val functions: List<String> = emptyList(),      // Built-in functions
    val constants: List<String> = emptyList()       // Built-in constants
)

/**
 * Regex patterns for syntax elements
 */
@Serializable
data class LanguagePatterns(
    val singleLineComment: String? = null,         // e.g., "//" or "#"
    val multiLineCommentStart: String? = null,     // e.g., "/*"
    val multiLineCommentEnd: String? = null,       // e.g., "*/"
    val stringDelimiters: List<String> = listOf("\"", "'"), // String delimiters
    val numberPattern: String = "\\b\\d+(\\.\\d+)?\\b",     // Number regex
    val identifierPattern: String = "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b", // Identifier regex
    val operatorPattern: String = "[+\\-*/=%<>!&|^~]",      // Operators
    val bracketPattern: String = "[\\[\\]{}()]",            // Brackets
    val functionCallPattern: String = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",  // Function calls
    val classPattern: String? = null,               // Class definition pattern
    val importPattern: String? = null,              // Import statement pattern
    val annotationPattern: String? = null           // Annotation pattern
)

/**
 * Color configuration for syntax elements
 */
@Serializable
data class LanguageColors(
    val keyword: String = "#569CD6",               // Blue
    val secondaryKeyword: String = "#C586C0",      // Purple
    val type: String = "#4EC9B0",                  // Teal
    val string: String = "#CE9178",                // Orange
    val number: String = "#B5CEA8",                // Light green
    val comment: String = "#6A9955",               // Green
    val operator: String = "#D4D4D4",              // Light gray
    val function: String = "#DCDCAA",              // Yellow
    val class_: String = "#4EC9B0",                // Teal
    val literal: String = "#569CD6",               // Blue
    val identifier: String = "#9CDCFE",            // Light blue
    val annotation: String = "#FFD700"             // Gold
)

/**
 * Language-specific features and behavior
 */
@Serializable
data class LanguageFeatures(
    val caseSensitive: Boolean = true,             // Case sensitive keywords
    val indentSize: Int = 4,                       // Default indentation
    val usesTabs: Boolean = false,                 // Use tabs vs spaces
    val autoIndent: Boolean = true,                // Auto-indent new lines
    val bracketMatching: Boolean = true,           // Enable bracket matching
    val lineNumbers: Boolean = true,               // Show line numbers
    val wordWrap: Boolean = false,                 // Enable word wrap
    val highlightCurrentLine: Boolean = true       // Highlight current line
)

/**
 * Supported programming languages
 */
enum class SupportedLanguage(val id: String, val displayName: String) {
    KOTLIN("kotlin", "Kotlin"),
    JAVA("java", "Java"),
    PYTHON("python", "Python"),
    JAVASCRIPT("javascript", "JavaScript"),
    TYPESCRIPT("typescript", "TypeScript"),
    CSHARP("csharp", "C#"),
    CPP("cpp", "C++"),
    C("c", "C"),
    HTML("html", "HTML"),
    CSS("css", "CSS"),
    JSON("json", "JSON"),
    XML("xml", "XML"),
    YAML("yaml", "YAML"),
    MARKDOWN("markdown", "Markdown"),
    PLAIN_TEXT("plaintext", "Plain Text");
    
    companion object {
        fun fromFileExtension(extension: String): SupportedLanguage {
            return when (extension.lowercase()) {
                "kt", "kts" -> KOTLIN
                "java" -> JAVA
                "py", "pyw" -> PYTHON
                "js", "mjs" -> JAVASCRIPT
                "ts" -> TYPESCRIPT
                "cs" -> CSHARP
                "cpp", "cxx", "cc" -> CPP
                "c", "h" -> C
                "html", "htm" -> HTML
                "css" -> CSS
                "json" -> JSON
                "xml" -> XML
                "yml", "yaml" -> YAML
                "md", "markdown" -> MARKDOWN
                else -> PLAIN_TEXT
            }
        }
        
        fun fromId(id: String): SupportedLanguage? {
            return values().find { it.id == id }
        }
    }
}
