package com.kotlintexteditor.syntax

import android.content.Context
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import com.kotlintexteditor.ui.editor.EditorLanguage
import kotlinx.coroutines.*

/**
 * Enhanced language manager that provides the best available syntax highlighting
 * for each supported language using Sora Editor's built-in capabilities
 */
class EnhancedLanguageManager private constructor(private val context: Context) {
    
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        @Volatile
        private var INSTANCE: EnhancedLanguageManager? = null
        
        fun getInstance(context: Context): EnhancedLanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EnhancedLanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Initialize the language manager
     */
    suspend fun initialize() {
        if (isInitialized) return
        
        withContext(Dispatchers.IO) {
            try {
                // Initialize any required components
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
                isInitialized = true // Continue anyway
            }
        }
    }
    
    /**
     * Get the best available language for the given editor language
     */
    fun getLanguageForEditor(editorLanguage: EditorLanguage): Language {
        return when (editorLanguage) {
            EditorLanguage.KOTLIN,
            EditorLanguage.JAVA -> {
                // Use built-in Java language which provides excellent syntax highlighting
                // for both Java and Kotlin (since Kotlin is similar enough)
                JavaLanguage()
            }
            
            EditorLanguage.PYTHON -> {
                // Try TextMate Python, fallback to enhanced empty language
                createEnhancedLanguageForPython()
            }
            
            EditorLanguage.JAVASCRIPT -> {
                // Try TextMate JavaScript, fallback to enhanced empty language
                createEnhancedLanguageForJavaScript()
            }
            
            EditorLanguage.TYPESCRIPT -> {
                // TypeScript is similar to JavaScript, use enhanced language
                createEnhancedLanguageForTypeScript()
            }
            
            EditorLanguage.HTML -> {
                // Use enhanced language for HTML
                createEnhancedLanguageForHTML()
            }
            
            EditorLanguage.CSS -> {
                // Use enhanced language for CSS
                createEnhancedLanguageForCSS()
            }
            
            EditorLanguage.JSON -> {
                // Use enhanced language for JSON
                createEnhancedLanguageForJSON()
            }
            
            EditorLanguage.XML -> {
                // Use enhanced language for XML
                createEnhancedLanguageForXML()
            }
            
            EditorLanguage.YAML -> {
                // Use enhanced language for YAML
                createEnhancedLanguageForYAML()
            }
            
            EditorLanguage.MARKDOWN -> {
                // Use enhanced language for Markdown
                createEnhancedLanguageForMarkdown()
            }
            
            EditorLanguage.CSHARP -> {
                // C# is similar to Java, use Java language as base
                JavaLanguage()
            }
            
            EditorLanguage.CPP -> {
                // Use enhanced language for C++
                createEnhancedLanguageForCPP()
            }
            
            EditorLanguage.PLAIN_TEXT -> {
                // Plain text uses empty language
                EmptyLanguage()
            }
        }
    }
    
    /**
     * Create enhanced Python language with basic syntax highlighting
     */
    private fun createEnhancedLanguageForPython(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.python", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced JavaScript language
     */
    private fun createEnhancedLanguageForJavaScript(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.js", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced TypeScript language
     */
    private fun createEnhancedLanguageForTypeScript(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.ts", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced HTML language
     */
    private fun createEnhancedLanguageForHTML(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("text.html.basic", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced CSS language
     */
    private fun createEnhancedLanguageForCSS(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.css", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced JSON language
     */
    private fun createEnhancedLanguageForJSON(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.json", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced XML language
     */
    private fun createEnhancedLanguageForXML(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("text.xml", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced YAML language
     */
    private fun createEnhancedLanguageForYAML(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.yaml", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced Markdown language
     */
    private fun createEnhancedLanguageForMarkdown(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("text.html.markdown", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Create enhanced C++ language
     */
    private fun createEnhancedLanguageForCPP(): Language {
        return try {
            // Try to use TextMate if available
            io.github.rosemoe.sora.langs.textmate.TextMateLanguage.create("source.cpp", true)
        } catch (e: Exception) {
            // Fallback to empty language
            EmptyLanguage()
        }
    }
    
    /**
     * Configure editor with appropriate language and theme
     */
    fun configureEditor(editor: CodeEditor, editorLanguage: EditorLanguage) {
        try {
            // Set the appropriate language
            val language = getLanguageForEditor(editorLanguage)
            editor.setEditorLanguage(language)
            
            // Apply our custom VS Code theme
            com.kotlintexteditor.ui.editor.WorkingVSCodeTheme.applyToEditor(editor)
            
            // Configure editor settings
            editor.apply {
                isEditable = true
                setLineNumberEnabled(true)
                setWordwrap(false)
                setTextSize(14f)
                setCursorBlinkPeriod(500)
                isHighlightCurrentBlock = true
                isBlockLineEnabled = true
                isHighlightCurrentLine = true
                tabWidth = 4
                
                // Enable magnifier
                getComponent(io.github.rosemoe.sora.widget.component.Magnifier::class.java)?.isEnabled = true
                
                // Force refresh
                post {
                    invalidate()
                    requestLayout()
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to basic configuration
            editor.setEditorLanguage(EmptyLanguage())
            com.kotlintexteditor.ui.editor.WorkingVSCodeTheme.applyToEditor(editor)
        }
    }
}
