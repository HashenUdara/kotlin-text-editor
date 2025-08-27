package com.kotlintexteditor.syntax

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.kotlintexteditor.ui.editor.EditorLanguage
import java.io.File
import java.io.IOException

/**
 * Manages JSON-based language configurations
 */
class JsonConfigurationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "JsonConfigManager"
        private const val CONFIG_DIR = "language_configs"
        private const val USER_CONFIG_DIR = "user_language_configs"
    }
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Load configuration for a specific language
     */
    suspend fun loadConfiguration(language: EditorLanguage): LanguageConfiguration? {
        return withContext(Dispatchers.IO) {
            try {
                // First try to load user configuration
                val userConfig = loadUserConfiguration(language)
                if (userConfig != null) {
                    Log.d(TAG, "Loaded user configuration for ${language.name}")
                    return@withContext userConfig
                }
                
                // Fall back to default configuration
                val defaultConfig = loadDefaultConfiguration(language)
                if (defaultConfig != null) {
                    Log.d(TAG, "Loaded default configuration for ${language.name}")
                    return@withContext defaultConfig
                }
                
                Log.w(TAG, "No configuration found for ${language.name}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading configuration for ${language.name}", e)
                null
            }
        }
    }
    
    /**
     * Save user configuration for a specific language
     */
    suspend fun saveConfiguration(language: EditorLanguage, configuration: LanguageConfiguration): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userConfigDir = File(context.filesDir, USER_CONFIG_DIR)
                if (!userConfigDir.exists()) {
                    userConfigDir.mkdirs()
                }
                
                val configFile = File(userConfigDir, "${getLanguageFileName(language)}.json")
                val jsonString = json.encodeToString(configuration)
                
                configFile.writeText(jsonString)
                Log.d(TAG, "Saved user configuration for ${language.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving configuration for ${language.name}", e)
                false
            }
        }
    }
    
    /**
     * Load configuration from JSON string
     */
    suspend fun loadConfigurationFromJson(jsonString: String): Result<LanguageConfiguration> {
        return withContext(Dispatchers.IO) {
            try {
                val configuration = json.decodeFromString<LanguageConfiguration>(jsonString)
                Result.success(configuration)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON configuration", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export configuration to JSON string
     */
    suspend fun exportConfigurationToJson(language: EditorLanguage): String? {
        return withContext(Dispatchers.IO) {
            try {
                val configuration = loadConfiguration(language)
                if (configuration != null) {
                    json.encodeToString(configuration)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting configuration for ${language.name}", e)
                null
            }
        }
    }
    
    /**
     * Get list of all available language configurations
     */
    suspend fun getAvailableLanguages(): List<EditorLanguage> {
        return withContext(Dispatchers.IO) {
            try {
                val languages = mutableListOf<EditorLanguage>()
                
                // Check default configurations in assets
                val assetManager = context.assets
                val configFiles = assetManager.list(CONFIG_DIR) ?: emptyArray()
                
                for (fileName in configFiles) {
                    if (fileName.endsWith(".json")) {
                        val languageName = fileName.removeSuffix(".json")
                        val language = getLanguageFromFileName(languageName)
                        if (language != null) {
                            languages.add(language)
                        }
                    }
                }
                
                // Add languages that have user configurations
                val userConfigDir = File(context.filesDir, USER_CONFIG_DIR)
                if (userConfigDir.exists()) {
                    val userConfigFiles = userConfigDir.listFiles { file ->
                        file.name.endsWith(".json")
                    } ?: emptyArray()
                    
                    for (file in userConfigFiles) {
                        val languageName = file.name.removeSuffix(".json")
                        val language = getLanguageFromFileName(languageName)
                        if (language != null && language !in languages) {
                            languages.add(language)
                        }
                    }
                }
                
                languages.sorted()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting available languages", e)
                emptyList()
            }
        }
    }
    
    /**
     * Reset language configuration to default
     */
    suspend fun resetToDefault(language: EditorLanguage): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userConfigDir = File(context.filesDir, USER_CONFIG_DIR)
                val configFile = File(userConfigDir, "${getLanguageFileName(language)}.json")
                
                if (configFile.exists()) {
                    configFile.delete()
                    Log.d(TAG, "Reset ${language.name} to default configuration")
                    true
                } else {
                    Log.d(TAG, "${language.name} already using default configuration")
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting ${language.name} configuration", e)
                false
            }
        }
    }
    
    /**
     * Check if language has user customizations
     */
    suspend fun hasUserCustomizations(language: EditorLanguage): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userConfigDir = File(context.filesDir, USER_CONFIG_DIR)
                val configFile = File(userConfigDir, "${getLanguageFileName(language)}.json")
                configFile.exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Get example JSON template for a language
     */
    suspend fun getExampleTemplate(language: EditorLanguage): String {
        return withContext(Dispatchers.IO) {
            try {
                // Try to load existing configuration as template
                val existing = loadConfiguration(language)
                if (existing != null) {
                    json.encodeToString(existing)
                } else {
                    // Return a basic template
                    createBasicTemplate(language)
                }
            } catch (e: Exception) {
                createBasicTemplate(language)
            }
        }
    }
    
    // Private helper methods
    
    private suspend fun loadDefaultConfiguration(language: EditorLanguage): LanguageConfiguration? {
        return try {
            val fileName = "${getLanguageFileName(language)}.json"
            val inputStream = context.assets.open("$CONFIG_DIR/$fileName")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<LanguageConfiguration>(jsonString)
        } catch (e: IOException) {
            Log.w(TAG, "No default configuration file for ${language.name}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading default configuration for ${language.name}", e)
            null
        }
    }
    
    private suspend fun loadUserConfiguration(language: EditorLanguage): LanguageConfiguration? {
        return try {
            val userConfigDir = File(context.filesDir, USER_CONFIG_DIR)
            val configFile = File(userConfigDir, "${getLanguageFileName(language)}.json")
            
            if (configFile.exists()) {
                val jsonString = configFile.readText()
                json.decodeFromString<LanguageConfiguration>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user configuration for ${language.name}", e)
            null
        }
    }
    
    private fun getLanguageFileName(language: EditorLanguage): String {
        return when (language) {
            EditorLanguage.KOTLIN -> "kotlin"
            EditorLanguage.JAVA -> "java"
            EditorLanguage.PYTHON -> "python"
            EditorLanguage.JAVASCRIPT -> "javascript"
            EditorLanguage.TYPESCRIPT -> "typescript"
            EditorLanguage.CSHARP -> "csharp"
            EditorLanguage.CPP -> "cpp"
            EditorLanguage.HTML -> "html"
            EditorLanguage.CSS -> "css"
            EditorLanguage.JSON -> "json"
            EditorLanguage.XML -> "xml"
            EditorLanguage.YAML -> "yaml"
            EditorLanguage.MARKDOWN -> "markdown"
            EditorLanguage.PLAIN_TEXT -> "plaintext"
        }
    }
    
    private fun getLanguageFromFileName(fileName: String): EditorLanguage? {
        return when (fileName.lowercase()) {
            "kotlin" -> EditorLanguage.KOTLIN
            "java" -> EditorLanguage.JAVA
            "python" -> EditorLanguage.PYTHON
            "javascript" -> EditorLanguage.JAVASCRIPT
            "typescript" -> EditorLanguage.TYPESCRIPT
            "csharp" -> EditorLanguage.CSHARP
            "cpp" -> EditorLanguage.CPP
            "html" -> EditorLanguage.HTML
            "css" -> EditorLanguage.CSS
            "json" -> EditorLanguage.JSON
            "xml" -> EditorLanguage.XML
            "yaml" -> EditorLanguage.YAML
            "markdown" -> EditorLanguage.MARKDOWN
            "plaintext" -> EditorLanguage.PLAIN_TEXT
            else -> null
        }
    }
    
    private fun createBasicTemplate(language: EditorLanguage): String {
        val basicConfig = LanguageConfiguration(
            name = language.name,
            fileExtensions = getDefaultExtensions(language),
            keywords = LanguageKeywords(),
            patterns = LanguagePatterns(),
            colors = null, // Use default colors
            features = LanguageFeatures()
        )
        return json.encodeToString(basicConfig)
    }
    
    private fun getDefaultExtensions(language: EditorLanguage): List<String> {
        return when (language) {
            EditorLanguage.KOTLIN -> listOf("kt", "kts")
            EditorLanguage.JAVA -> listOf("java")
            EditorLanguage.PYTHON -> listOf("py", "pyw")
            EditorLanguage.JAVASCRIPT -> listOf("js", "mjs", "jsx")
            EditorLanguage.TYPESCRIPT -> listOf("ts", "tsx")
            EditorLanguage.CSHARP -> listOf("cs")
            EditorLanguage.CPP -> listOf("cpp", "cxx", "cc", "c", "h", "hpp")
            EditorLanguage.HTML -> listOf("html", "htm")
            EditorLanguage.CSS -> listOf("css")
            EditorLanguage.JSON -> listOf("json")
            EditorLanguage.XML -> listOf("xml")
            EditorLanguage.YAML -> listOf("yml", "yaml")
            EditorLanguage.MARKDOWN -> listOf("md", "markdown")
            EditorLanguage.PLAIN_TEXT -> listOf("txt")
        }
    }
}
