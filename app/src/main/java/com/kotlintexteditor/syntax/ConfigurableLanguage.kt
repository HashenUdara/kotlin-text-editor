package com.kotlintexteditor.syntax

import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.text.ContentReference

/**
 * A simplified configurable language implementation that uses LanguageConfiguration
 * to provide basic syntax highlighting for various programming languages
 */
class ConfigurableLanguage(
    private val configuration: LanguageConfiguration
) : EmptyLanguage() {
    
    override fun getInterruptionLevel(): Int {
        return INTERRUPTION_LEVEL_STRONG
    }
    
    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        return try {
            val lineText = content.getLine(line).toString()
            when {
                lineText.trimEnd().endsWith("{") -> configuration.features.indentSize
                lineText.trimEnd().endsWith("(") -> configuration.features.indentSize
                lineText.trimEnd().endsWith("[") -> configuration.features.indentSize
                lineText.trimEnd().endsWith(":") && configuration.name == "Python" -> configuration.features.indentSize
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    override fun useTab(): Boolean {
        return configuration.features.usesTabs
    }
}


