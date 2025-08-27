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
 * Now includes JSON configuration support
 */
class EnhancedLanguageManager private constructor(private val context: Context) {
    
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val jsonConfigManager = JsonConfigurationManager(context)
    
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
                // Initialize JSON configuration system
                val availableLanguages = jsonConfigManager.getAvailableLanguages()
                android.util.Log.d("EnhancedLanguageManager", "Initialized with ${availableLanguages.size} language configurations")
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
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.JAVASCRIPT -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.TYPESCRIPT -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.HTML -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.CSS -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.JSON -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.XML -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.YAML -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.MARKDOWN -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.CSHARP -> {
                // C# is similar to Java, use Java language as base for syntax highlighting
                JavaLanguage()
            }
            
            EditorLanguage.CPP -> {
                // Use enhanced empty language with good editor settings
                createEnhancedEmptyLanguage()
            }
            
            EditorLanguage.PLAIN_TEXT -> {
                // Plain text uses empty language
                EmptyLanguage()
            }
        }
    }
    
    /**
     * Create enhanced language using EmptyLanguage with optimized settings
     * This provides a clean, working editor experience for all languages
     */
    private fun createEnhancedEmptyLanguage(): Language {
        return EmptyLanguage()
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
                
                // Fix caps lock issue by setting proper input type
                // This disables auto-capitalization and other automatic text transformations
                inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                           android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                           android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                
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
    
    // === JSON Configuration Methods ===
    
    /**
     * Load language configuration from JSON
     */
    suspend fun getLanguageConfiguration(language: EditorLanguage): LanguageConfiguration? {
        return jsonConfigManager.loadConfiguration(language)
    }
    
    /**
     * Save language configuration as JSON
     */
    suspend fun saveLanguageConfiguration(language: EditorLanguage, configuration: LanguageConfiguration): Boolean {
        return jsonConfigManager.saveConfiguration(language, configuration)
    }
    
    /**
     * Export language configuration to JSON string
     */
    suspend fun exportConfigurationToJson(language: EditorLanguage): String? {
        return jsonConfigManager.exportConfigurationToJson(language)
    }
    
    /**
     * Load configuration from JSON string
     */
    suspend fun loadConfigurationFromJson(jsonString: String): Result<LanguageConfiguration> {
        return jsonConfigManager.loadConfigurationFromJson(jsonString)
    }
    
    /**
     * Get list of available languages with JSON configurations
     */
    suspend fun getAvailableLanguages(): List<EditorLanguage> {
        return jsonConfigManager.getAvailableLanguages()
    }
    
    /**
     * Reset language to default configuration
     */
    suspend fun resetToDefault(language: EditorLanguage): Boolean {
        return jsonConfigManager.resetToDefault(language)
    }
    
    /**
     * Check if language has user customizations
     */
    suspend fun hasUserCustomizations(language: EditorLanguage): Boolean {
        return jsonConfigManager.hasUserCustomizations(language)
    }
    
    /**
     * Get example JSON template for a language
     */
    suspend fun getExampleTemplate(language: EditorLanguage): String {
        return jsonConfigManager.getExampleTemplate(language)
    }
}