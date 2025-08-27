package com.kotlintexteditor.ui.editor

import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Simple Java syntax language (reuses Kotlin highlighting)
 */
class JavaSyntaxLanguage : EmptyLanguage() {
    
    override fun getInterruptionLevel(): Int {
        return INTERRUPTION_LEVEL_STRONG
    }
    
    override fun getIndentAdvance(content: io.github.rosemoe.sora.text.ContentReference, line: Int, column: Int): Int {
        val text = content.getLine(line).toString()
        return when {
            text.trimEnd().endsWith("{") -> 4
            text.trimEnd().endsWith("(") -> 4
            else -> 0
        }
    }
    
    override fun useTab(): Boolean = false
}

/**
 * VS Code-inspired color scheme
 */
object VSCodeTheme {
    
    // VS Code Dark+ theme colors
    object Colors {
        const val BACKGROUND = 0xFF1E1E1E.toInt()
        const val TEXT_NORMAL = 0xFFD4D4D4.toInt()
        const val LINE_NUMBER = 0xFF858585.toInt()
        const val LINE_NUMBER_BACKGROUND = 0xFF1E1E1E.toInt()
        const val SELECTION = 0xFF264F78.toInt()
        const val CURSOR = 0xFFAEAFAD.toInt()
        const val CURRENT_LINE = 0xFF2A2D2E.toInt()
        
        // Syntax highlighting colors (VS Code Dark+ inspired)
        const val KEYWORD = 0xFF569CD6.toInt()        // Blue - keywords (fun, class, val, var)
        const val STRING = 0xFFCE9178.toInt()         // Orange - string literals
        const val COMMENT = 0xFF6A9955.toInt()        // Green - comments
        const val NUMBER = 0xFFB5CEA8.toInt()         // Light green - numbers
        const val TYPE = 0xFF4EC9B0.toInt()           // Teal - types (String, Int, etc.)
        const val FUNCTION = 0xFFDCDCAA.toInt()       // Yellow - function names
        const val OPERATOR = 0xFFD4D4D4.toInt()       // White - operators
        const val BRACKET = 0xFFFFD700.toInt()        // Gold - brackets and delimiters
        const val ANNOTATION = 0xFFD2A8F4.toInt()     // Purple - annotations
        const val IMPORT = 0xFF569CD6.toInt()         // Blue - imports
        
        // Error and warning colors
        const val ERROR = 0xFFFF5555.toInt()
        const val WARNING = 0xFFFFAA00.toInt()
        const val INFO = 0xFF55FF55.toInt()
        
        // UI colors
        const val SCROLLBAR = 0xFF424242.toInt()
        const val SCROLLBAR_PRESSED = 0xFF686868.toInt()
        const val SCROLLBAR_TRACK = 0xFF2A2A2A.toInt()
    }
    
    /**
     * Apply VS Code Dark+ theme to editor
     */
    fun applyToEditor(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        val colorScheme = EditorColorScheme()
        
        // Background and text colors (using actual available constants)
        try {
            colorScheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, Colors.BACKGROUND)
            colorScheme.setColor(EditorColorScheme.TEXT_NORMAL, Colors.TEXT_NORMAL)
            
            // Line numbers
            colorScheme.setColor(EditorColorScheme.LINE_NUMBER, Colors.LINE_NUMBER)
            colorScheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, Colors.LINE_NUMBER_BACKGROUND)
            
            // Selection and cursor
            colorScheme.setColor(EditorColorScheme.SELECTED_TEXT_BACKGROUND, Colors.SELECTION)
            colorScheme.setColor(EditorColorScheme.SELECTION_INSERT, Colors.CURSOR)
            
            // Current line
            colorScheme.setColor(EditorColorScheme.CURRENT_LINE, Colors.CURRENT_LINE)
            
            // Syntax highlighting
            colorScheme.setColor(EditorColorScheme.KEYWORD, Colors.KEYWORD)
            colorScheme.setColor(EditorColorScheme.LITERAL, Colors.STRING)
            colorScheme.setColor(EditorColorScheme.COMMENT, Colors.COMMENT)
            colorScheme.setColor(EditorColorScheme.IDENTIFIER_NAME, Colors.TEXT_NORMAL)
            colorScheme.setColor(EditorColorScheme.IDENTIFIER_VAR, Colors.TEXT_NORMAL)
            colorScheme.setColor(EditorColorScheme.OPERATOR, Colors.OPERATOR)
            
            // Brackets and delimiters
            colorScheme.setColor(EditorColorScheme.BLOCK_LINE, Colors.BRACKET)
            colorScheme.setColor(EditorColorScheme.BLOCK_LINE_CURRENT, Colors.BRACKET)
            
            // Problems (errors, warnings)
            colorScheme.setColor(EditorColorScheme.PROBLEM_ERROR, Colors.ERROR)
            colorScheme.setColor(EditorColorScheme.PROBLEM_WARNING, Colors.WARNING)
            colorScheme.setColor(EditorColorScheme.PROBLEM_TYPO, Colors.INFO)
            
            // Scrollbar
            colorScheme.setColor(EditorColorScheme.SCROLL_BAR_THUMB, Colors.SCROLLBAR)
            colorScheme.setColor(EditorColorScheme.SCROLL_BAR_THUMB_PRESSED, Colors.SCROLLBAR_PRESSED)
            colorScheme.setColor(EditorColorScheme.SCROLL_BAR_TRACK, Colors.SCROLLBAR_TRACK)
            
            // Auto-completion popup - use basic colors if specific ones aren't available
            // Using a fallback since AUTO_COMP_PANEL_BG might not be available in this version
            
        } catch (e: Exception) {
            // If any color constants are not available, ignore and continue
        }
        
        // Apply to editor
        editor.colorScheme = colorScheme
    }
}

/**
 * Enhanced editor configuration utilities
 */
object EditorConfigUtils {
    
    /**
     * Configure editor for optimal Kotlin editing experience
     */
    fun configureForKotlin(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        WorkingEditorConfig.configureForKotlin(editor)
    }
    
    /**
     * Configure editor for Java
     */
    fun configureForJava(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        WorkingEditorConfig.configureForJava(editor)
    }
    
    /**
     * Configure editor for plain text
     */
    fun configureForPlainText(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        WorkingEditorConfig.configureForPlainText(editor)
    }
}
