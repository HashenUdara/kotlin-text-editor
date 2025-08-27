package com.kotlintexteditor.syntax

import android.content.Context
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.langs.java.JavaLanguage
import com.kotlintexteditor.ui.editor.WorkingVSCodeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manages configurable syntax highlighting for the code editor
 */
class ConfigurableEditorManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val configurationManager = LanguageConfigurationManager(context)
    private var isInitialized = false
    
    companion object {
        @Volatile
        private var INSTANCE: ConfigurableEditorManager? = null
        
        fun getInstance(context: Context): ConfigurableEditorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigurableEditorManager(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
    }
    
    /**
     * Initialize the manager (should be called once at app startup)
     */
    suspend fun initialize() {
        if (!isInitialized) {
            configurationManager.initialize()
            isInitialized = true
        }
    }
    
    /**
     * Configure editor for a specific language
     */
    fun configureEditor(editor: CodeEditor, language: SupportedLanguage) {
        scope.launch {
            try {
                val configuration = configurationManager.getConfiguration(language)
                
                if (configuration != null) {
                    // Use appropriate language based on type
                    when (language) {
                        SupportedLanguage.KOTLIN, SupportedLanguage.JAVA -> {
                            // Use built-in Java language for Kotlin/Java (best compatibility)
                            editor.setEditorLanguage(JavaLanguage())
                        }
                        else -> {
                            // Use configurable language for other types
                            val configurableLanguage = ConfigurableLanguage(configuration)
                            editor.setEditorLanguage(configurableLanguage)
                        }
                    }
                    
                    // Apply custom color scheme if provided
                    configuration.colors?.let { colors ->
                        applyCustomColorScheme(editor, colors)
                    } ?: run {
                        // Apply default VS Code theme
                        WorkingVSCodeTheme.applyToEditor(editor)
                    }
                    
                    // Apply language-specific features
                    applyLanguageFeatures(editor, configuration.features)
                    
                } else {
                    // Fallback to default configuration
                    configureDefaultEditor(editor, language)
                }
                
                // Force refresh
                editor.post {
                    editor.invalidate()
                    editor.requestLayout()
                }
                
            } catch (e: Exception) {
                // Fallback to default configuration on error
                configureDefaultEditor(editor, language)
            }
        }
    }
    
    /**
     * Configure editor by file extension
     */
    fun configureEditorByExtension(editor: CodeEditor, extension: String) {
        val language = SupportedLanguage.fromFileExtension(extension)
        configureEditor(editor, language)
    }
    
    /**
     * Get available language configurations
     */
    fun getAvailableLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }
    
    /**
     * Get configuration for a language
     */
    fun getLanguageConfiguration(language: SupportedLanguage): LanguageConfiguration? {
        return configurationManager.getConfiguration(language)
    }
    
    /**
     * Update configuration for a language
     */
    fun updateLanguageConfiguration(language: SupportedLanguage, configuration: LanguageConfiguration) {
        configurationManager.setConfiguration(language, configuration)
    }
    
    /**
     * Load configuration from JSON
     */
    suspend fun loadConfigurationFromJson(json: String): Result<LanguageConfiguration> {
        return configurationManager.loadConfigurationFromJson(json)
    }
    
    /**
     * Export configuration to JSON
     */
    fun exportConfigurationToJson(language: SupportedLanguage): String? {
        return configurationManager.exportConfigurationToJson(language)
    }
    
    /**
     * Apply custom color scheme from configuration
     */
    private fun applyCustomColorScheme(editor: CodeEditor, colors: LanguageColors) {
        try {
            val colorScheme = EditorColorScheme()
            
            // Parse color strings to integers
            val keywordColor = parseColor(colors.keyword)
            val secondaryKeywordColor = parseColor(colors.secondaryKeyword)
            val typeColor = parseColor(colors.type)
            val stringColor = parseColor(colors.string)
            val numberColor = parseColor(colors.number)
            val commentColor = parseColor(colors.comment)
            val operatorColor = parseColor(colors.operator)
            val functionColor = parseColor(colors.function)
            val classColor = parseColor(colors.class_)
            val literalColor = parseColor(colors.literal)
            val identifierColor = parseColor(colors.identifier)
            val annotationColor = parseColor(colors.annotation)
            
            // Apply colors to color scheme
            colorScheme.setColor(EditorColorScheme.KEYWORD, keywordColor)
            colorScheme.setColor(EditorColorScheme.IDENTIFIER_NAME, functionColor)
            colorScheme.setColor(EditorColorScheme.IDENTIFIER_VAR, identifierColor)
            colorScheme.setColor(EditorColorScheme.LITERAL, stringColor)
            colorScheme.setColor(EditorColorScheme.COMMENT, commentColor)
            colorScheme.setColor(EditorColorScheme.OPERATOR, operatorColor)
            
            // Apply base theme colors (keep existing ones from VS Code theme)
            WorkingVSCodeTheme.applyToEditor(editor)
            
            // Override with custom colors
            editor.colorScheme = colorScheme
            
        } catch (e: Exception) {
            // Fallback to default theme on error
            WorkingVSCodeTheme.applyToEditor(editor)
        }
    }
    
    /**
     * Apply language-specific features to editor
     */
    private fun applyLanguageFeatures(editor: CodeEditor, features: LanguageFeatures) {
        try {
            // Configure editor behavior
            editor.isEditable = true
            editor.setLineNumberEnabled(features.lineNumbers)
            editor.setWordwrap(features.wordWrap)
            editor.isHighlightCurrentLine = features.highlightCurrentLine
            editor.isHighlightCurrentBlock = features.bracketMatching
            editor.isBlockLineEnabled = features.bracketMatching
            
            // Configure indentation
            editor.tabWidth = features.indentSize
            
            // Configure text appearance
            editor.setTextSize(14f)
            editor.setCursorBlinkPeriod(500)
            
            // Enable magnifier
            editor.getComponent(io.github.rosemoe.sora.widget.component.Magnifier::class.java)?.isEnabled = true
            
        } catch (e: Exception) {
            // Handle feature application errors gracefully
        }
    }
    
    /**
     * Configure editor with default settings for fallback
     */
    private fun configureDefaultEditor(editor: CodeEditor, language: SupportedLanguage) {
        try {
            when (language) {
                SupportedLanguage.KOTLIN, SupportedLanguage.JAVA -> {
                    // Use built-in Java language for Kotlin/Java
                    editor.setEditorLanguage(JavaLanguage())
                }
                else -> {
                    // Use empty language for others
                    editor.setEditorLanguage(io.github.rosemoe.sora.lang.EmptyLanguage())
                }
            }
            
            // Apply default VS Code theme
            WorkingVSCodeTheme.applyToEditor(editor)
            
            // Apply default settings
            editor.isEditable = true
            editor.setLineNumberEnabled(true)
            editor.setWordwrap(false)
            editor.setTextSize(14f)
            editor.setCursorBlinkPeriod(500)
            editor.isHighlightCurrentBlock = true
            editor.isBlockLineEnabled = true
            editor.isHighlightCurrentLine = true
            editor.tabWidth = 4
            
            editor.getComponent(io.github.rosemoe.sora.widget.component.Magnifier::class.java)?.isEnabled = true
            
        } catch (e: Exception) {
            // Ultimate fallback - just apply basic settings
            editor.isEditable = true
            editor.setLineNumberEnabled(true)
        }
    }
    
    /**
     * Parse color string to integer (supports #RRGGBB format)
     */
    private fun parseColor(colorString: String): Int {
        return try {
            if (colorString.startsWith("#")) {
                android.graphics.Color.parseColor(colorString)
            } else {
                // Try parsing as hex without #
                android.graphics.Color.parseColor("#$colorString")
            }
        } catch (e: Exception) {
            // Default to white on parse error
            android.graphics.Color.WHITE
        }
    }
    
    /**
     * Reset to default configurations
     */
    suspend fun resetToDefaults() {
        configurationManager.initialize()
    }
    
    /**
     * Check if manager is initialized
     */
    fun isInitialized(): Boolean = isInitialized
}
