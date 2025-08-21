package com.kotlintexteditor.syntax

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Manages language configurations for syntax highlighting
 */
class LanguageConfigurationManager(private val context: Context) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
    
    private val configurations = mutableMapOf<SupportedLanguage, LanguageConfiguration>()
    
    /**
     * Initialize with default configurations
     */
    suspend fun initialize() {
        loadDefaultConfigurations()
    }
    
    /**
     * Get configuration for a specific language
     */
    fun getConfiguration(language: SupportedLanguage): LanguageConfiguration? {
        return configurations[language]
    }
    
    /**
     * Get configuration by file extension
     */
    fun getConfigurationByExtension(extension: String): LanguageConfiguration? {
        val language = SupportedLanguage.fromFileExtension(extension)
        return getConfiguration(language)
    }
    
    /**
     * Load configuration from JSON string
     */
    suspend fun loadConfigurationFromJson(json: String): Result<LanguageConfiguration> {
        return withContext(Dispatchers.IO) {
            try {
                val config = this@LanguageConfigurationManager.json.decodeFromString<LanguageConfiguration>(json)
                Result.success(config)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Save configuration for a language
     */
    fun setConfiguration(language: SupportedLanguage, configuration: LanguageConfiguration) {
        configurations[language] = configuration
    }
    
    /**
     * Load configuration from assets
     */
    private suspend fun loadConfigurationFromAssets(fileName: String): LanguageConfiguration? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("syntax/$fileName").bufferedReader().use { it.readText() }
                json.decodeFromString<LanguageConfiguration>(jsonString)
            } catch (e: IOException) {
                null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Load all default configurations
     */
    private suspend fun loadDefaultConfigurations() {
        // Load configurations for each supported language
        loadDefaultKotlinConfiguration()
        loadDefaultJavaConfiguration()
        loadDefaultPythonConfiguration()
        loadDefaultJavaScriptConfiguration()
        loadDefaultTypeScriptConfiguration()
        loadDefaultCSharpConfiguration()
        loadDefaultCppConfiguration()
        loadDefaultHtmlConfiguration()
        loadDefaultCssConfiguration()
        loadDefaultJsonConfiguration()
        loadDefaultXmlConfiguration()
        loadDefaultMarkdownConfiguration()
        
        // Try to load from assets (if available)
        SupportedLanguage.values().forEach { language ->
            val assetConfig = loadConfigurationFromAssets("${language.id}.json")
            if (assetConfig != null) {
                configurations[language] = assetConfig
            }
        }
    }
    
    /**
     * Default Kotlin configuration
     */
    private fun loadDefaultKotlinConfiguration() {
        val config = LanguageConfiguration(
            name = "Kotlin",
            fileExtensions = listOf("kt", "kts"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
                    "companion", "const", "constructor", "continue", "crossinline", "data",
                    "do", "dynamic", "else", "enum", "expect", "external", "false", "final",
                    "finally", "for", "fun", "get", "if", "import", "in", "infix", "init",
                    "inline", "inner", "interface", "internal", "is", "lateinit", "noinline",
                    "null", "object", "open", "operator", "out", "override", "package",
                    "private", "protected", "public", "reified", "return", "sealed", "set",
                    "super", "suspend", "tailrec", "this", "throw", "true", "try", "typealias",
                    "typeof", "val", "var", "vararg", "when", "where", "while"
                ),
                types = listOf(
                    "Any", "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long",
                    "Nothing", "Short", "String", "Unit", "Array", "List", "Map", "Set",
                    "MutableList", "MutableMap", "MutableSet"
                ),
                literals = listOf("true", "false", "null"),
                functions = listOf(
                    "println", "print", "readLine", "TODO", "run", "let", "also", "apply",
                    "with", "takeIf", "takeUnless", "repeat", "listOf", "mapOf", "setOf",
                    "arrayOf", "emptyList", "emptyMap", "emptySet"
                )
            ),
            patterns = LanguagePatterns(
                singleLineComment = "//",
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'"),
                functionCallPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_]*)",
                importPattern = "\\bimport\\s+([a-zA-Z0-9_.]+)",
                annotationPattern = "@[a-zA-Z][a-zA-Z0-9_]*"
            ),
            features = LanguageFeatures(
                indentSize = 4,
                usesTabs = false,
                autoIndent = true,
                bracketMatching = true
            )
        )
        configurations[SupportedLanguage.KOTLIN] = config
    }
    
    /**
     * Default Java configuration
     */
    private fun loadDefaultJavaConfiguration() {
        val config = LanguageConfiguration(
            name = "Java",
            fileExtensions = listOf("java"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                    "class", "const", "continue", "default", "do", "double", "else", "enum",
                    "extends", "final", "finally", "float", "for", "goto", "if", "implements",
                    "import", "instanceof", "int", "interface", "long", "native", "new",
                    "package", "private", "protected", "public", "return", "short", "static",
                    "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
                    "transient", "try", "void", "volatile", "while"
                ),
                types = listOf(
                    "boolean", "byte", "char", "double", "float", "int", "long", "short",
                    "String", "Object", "Boolean", "Byte", "Character", "Double", "Float",
                    "Integer", "Long", "Short", "ArrayList", "HashMap", "HashSet"
                ),
                literals = listOf("true", "false", "null"),
                functions = listOf(
                    "System.out.println", "System.out.print", "toString", "equals", "hashCode",
                    "length", "size", "isEmpty", "contains", "add", "remove", "get", "put"
                )
            ),
            patterns = LanguagePatterns(
                singleLineComment = "//",
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'"),
                functionCallPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_]*)",
                importPattern = "\\bimport\\s+([a-zA-Z0-9_.]+)",
                annotationPattern = "@[a-zA-Z][a-zA-Z0-9_]*"
            ),
            features = LanguageFeatures(
                indentSize = 4,
                usesTabs = false,
                autoIndent = true,
                bracketMatching = true
            )
        )
        configurations[SupportedLanguage.JAVA] = config
    }
    
    /**
     * Default Python configuration
     */
    private fun loadDefaultPythonConfiguration() {
        val config = LanguageConfiguration(
            name = "Python",
            fileExtensions = listOf("py", "pyw"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "and", "as", "assert", "break", "class", "continue", "def", "del", "elif",
                    "else", "except", "exec", "finally", "for", "from", "global", "if",
                    "import", "in", "is", "lambda", "not", "or", "pass", "print", "raise",
                    "return", "try", "while", "with", "yield", "async", "await", "nonlocal"
                ),
                types = listOf(
                    "bool", "int", "float", "complex", "str", "bytes", "bytearray", "list",
                    "tuple", "range", "dict", "set", "frozenset", "type", "object"
                ),
                literals = listOf("True", "False", "None"),
                functions = listOf(
                    "print", "input", "len", "range", "enumerate", "zip", "map", "filter",
                    "sum", "min", "max", "abs", "round", "sorted", "reversed", "any", "all",
                    "open", "type", "isinstance", "hasattr", "getattr", "setattr"
                )
            ),
            patterns = LanguagePatterns(
                singleLineComment = "#",
                multiLineCommentStart = "\"\"\"",
                multiLineCommentEnd = "\"\"\"",
                stringDelimiters = listOf("\"", "'", "\"\"\"", "'''"),
                functionCallPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_]*)",
                importPattern = "\\b(import|from)\\s+([a-zA-Z0-9_.]+)"
            ),
            features = LanguageFeatures(
                indentSize = 4,
                usesTabs = false,
                autoIndent = true,
                bracketMatching = true
            )
        )
        configurations[SupportedLanguage.PYTHON] = config
    }
    
    /**
     * Default JavaScript configuration
     */
    private fun loadDefaultJavaScriptConfiguration() {
        val config = LanguageConfiguration(
            name = "JavaScript",
            fileExtensions = listOf("js", "mjs"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
                    "char", "class", "const", "continue", "debugger", "default", "delete", "do",
                    "double", "else", "enum", "eval", "export", "extends", "false", "final",
                    "finally", "float", "for", "function", "goto", "if", "implements", "import",
                    "in", "instanceof", "int", "interface", "let", "long", "native", "new",
                    "null", "package", "private", "protected", "public", "return", "short",
                    "static", "super", "switch", "synchronized", "this", "throw", "throws",
                    "transient", "true", "try", "typeof", "var", "void", "volatile", "while",
                    "with", "yield", "async"
                ),
                types = listOf(
                    "Array", "Boolean", "Date", "Error", "Function", "Number", "Object",
                    "RegExp", "String", "Symbol", "Promise", "Map", "Set", "WeakMap", "WeakSet"
                ),
                literals = listOf("true", "false", "null", "undefined"),
                functions = listOf(
                    "console.log", "console.error", "console.warn", "alert", "confirm", "prompt",
                    "parseInt", "parseFloat", "isNaN", "isFinite", "encodeURI", "decodeURI",
                    "setTimeout", "setInterval", "clearTimeout", "clearInterval"
                )
            ),
            patterns = LanguagePatterns(
                singleLineComment = "//",
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'", "`"),
                functionCallPattern = "\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_$]*)",
                importPattern = "\\b(import|from)\\s+([a-zA-Z0-9_./'\"]+)"
            ),
            features = LanguageFeatures(
                indentSize = 2,
                usesTabs = false,
                autoIndent = true,
                bracketMatching = true
            )
        )
        configurations[SupportedLanguage.JAVASCRIPT] = config
    }
    
    // Additional default configurations for other languages (abbreviated for space)
    private fun loadDefaultTypeScriptConfiguration() {
        // Similar to JavaScript but with additional TypeScript keywords
        val jsConfig = configurations[SupportedLanguage.JAVASCRIPT] ?: return loadDefaultJavaScriptConfiguration()
        val config = jsConfig.copy(
            name = "TypeScript",
            fileExtensions = listOf("ts"),
            keywords = jsConfig.keywords.copy(
                primary = jsConfig.keywords.primary + listOf(
                    "type", "interface", "namespace", "module", "declare", "abstract",
                    "readonly", "keyof", "infer", "never", "unknown", "any"
                ),
                types = jsConfig.keywords.types + listOf(
                    "string", "number", "boolean", "void", "any", "unknown", "never",
                    "object", "bigint", "symbol"
                )
            )
        )
        configurations[SupportedLanguage.TYPESCRIPT] = config
    }
    
    private fun loadDefaultCSharpConfiguration() {
        val config = LanguageConfiguration(
            name = "C#",
            fileExtensions = listOf("cs"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char",
                    "checked", "class", "const", "continue", "decimal", "default", "delegate",
                    "do", "double", "else", "enum", "event", "explicit", "extern", "false",
                    "finally", "fixed", "float", "for", "foreach", "goto", "if", "implicit",
                    "in", "int", "interface", "internal", "is", "lock", "long", "namespace",
                    "new", "null", "object", "operator", "out", "override", "params", "private",
                    "protected", "public", "readonly", "ref", "return", "sbyte", "sealed",
                    "short", "sizeof", "stackalloc", "static", "string", "struct", "switch",
                    "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked",
                    "unsafe", "ushort", "using", "virtual", "void", "volatile", "while"
                ),
                types = listOf(
                    "bool", "byte", "sbyte", "char", "decimal", "double", "float", "int",
                    "uint", "long", "ulong", "short", "ushort", "object", "string", "var",
                    "dynamic", "List", "Dictionary", "Array", "IEnumerable", "IList"
                ),
                literals = listOf("true", "false", "null")
            ),
            patterns = LanguagePatterns(
                singleLineComment = "//",
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'"),
                functionCallPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_]*)",
                importPattern = "\\busing\\s+([a-zA-Z0-9_.]+)"
            )
        )
        configurations[SupportedLanguage.CSHARP] = config
    }
    
    private fun loadDefaultCppConfiguration() {
        val config = LanguageConfiguration(
            name = "C++",
            fileExtensions = listOf("cpp", "cxx", "cc", "c", "h", "hpp"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "alignas", "alignof", "and", "and_eq", "asm", "auto", "bitand", "bitor",
                    "bool", "break", "case", "catch", "char", "char16_t", "char32_t", "class",
                    "compl", "const", "constexpr", "const_cast", "continue", "decltype",
                    "default", "delete", "do", "double", "dynamic_cast", "else", "enum",
                    "explicit", "export", "extern", "false", "float", "for", "friend", "goto",
                    "if", "inline", "int", "long", "mutable", "namespace", "new", "noexcept",
                    "not", "not_eq", "nullptr", "operator", "or", "or_eq", "private", "protected",
                    "public", "register", "reinterpret_cast", "return", "short", "signed",
                    "sizeof", "static", "static_assert", "static_cast", "struct", "switch",
                    "template", "this", "thread_local", "throw", "true", "try", "typedef",
                    "typeid", "typename", "union", "unsigned", "using", "virtual", "void",
                    "volatile", "wchar_t", "while", "xor", "xor_eq"
                ),
                types = listOf(
                    "bool", "char", "wchar_t", "char16_t", "char32_t", "short", "int", "long",
                    "float", "double", "void", "auto", "size_t", "string", "vector", "map",
                    "set", "list", "deque", "queue", "stack", "pair"
                ),
                literals = listOf("true", "false", "nullptr")
            ),
            patterns = LanguagePatterns(
                singleLineComment = "//",
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'"),
                functionCallPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(",
                classPattern = "\\bclass\\s+([A-Z][a-zA-Z0-9_]*)",
                importPattern = "\\#include\\s*[<\"]([a-zA-Z0-9_./<>]+)[\">]"
            )
        )
        configurations[SupportedLanguage.CPP] = config
        configurations[SupportedLanguage.C] = config
    }
    
    private fun loadDefaultHtmlConfiguration() {
        val config = LanguageConfiguration(
            name = "HTML",
            fileExtensions = listOf("html", "htm"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "html", "head", "title", "body", "div", "span", "p", "a", "img", "ul", "ol",
                    "li", "table", "tr", "td", "th", "form", "input", "button", "select", "option",
                    "textarea", "h1", "h2", "h3", "h4", "h5", "h6", "br", "hr", "meta", "link",
                    "script", "style", "noscript", "iframe", "embed", "object", "video", "audio",
                    "canvas", "svg", "nav", "header", "footer", "section", "article", "aside",
                    "main", "figure", "figcaption", "details", "summary", "mark", "time"
                )
            ),
            patterns = LanguagePatterns(
                singleLineComment = null,
                multiLineCommentStart = "<!--",
                multiLineCommentEnd = "-->",
                stringDelimiters = listOf("\"", "'")
            )
        )
        configurations[SupportedLanguage.HTML] = config
    }
    
    private fun loadDefaultCssConfiguration() {
        val config = LanguageConfiguration(
            name = "CSS",
            fileExtensions = listOf("css"),
            keywords = LanguageKeywords(
                primary = listOf(
                    "color", "background", "border", "margin", "padding", "width", "height",
                    "font", "text", "display", "position", "top", "right", "bottom", "left",
                    "z-index", "overflow", "float", "clear", "visibility", "opacity", "cursor",
                    "transform", "transition", "animation", "flex", "grid", "align", "justify"
                ),
                literals = listOf("inherit", "initial", "unset", "none", "auto", "normal")
            ),
            patterns = LanguagePatterns(
                singleLineComment = null,
                multiLineCommentStart = "/*",
                multiLineCommentEnd = "*/",
                stringDelimiters = listOf("\"", "'")
            )
        )
        configurations[SupportedLanguage.CSS] = config
    }
    
    private fun loadDefaultJsonConfiguration() {
        val config = LanguageConfiguration(
            name = "JSON",
            fileExtensions = listOf("json"),
            keywords = LanguageKeywords(
                literals = listOf("true", "false", "null")
            ),
            patterns = LanguagePatterns(
                stringDelimiters = listOf("\""),
                numberPattern = "-?\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b"
            )
        )
        configurations[SupportedLanguage.JSON] = config
    }
    
    private fun loadDefaultXmlConfiguration() {
        val config = LanguageConfiguration(
            name = "XML",
            fileExtensions = listOf("xml"),
            keywords = LanguageKeywords(),
            patterns = LanguagePatterns(
                multiLineCommentStart = "<!--",
                multiLineCommentEnd = "-->",
                stringDelimiters = listOf("\"", "'")
            )
        )
        configurations[SupportedLanguage.XML] = config
    }
    
    private fun loadDefaultMarkdownConfiguration() {
        val config = LanguageConfiguration(
            name = "Markdown",
            fileExtensions = listOf("md", "markdown"),
            keywords = LanguageKeywords(),
            patterns = LanguagePatterns(
                stringDelimiters = listOf("`", "```")
            )
        )
        configurations[SupportedLanguage.MARKDOWN] = config
    }
    
    /**
     * Get all available configurations
     */
    fun getAllConfigurations(): Map<SupportedLanguage, LanguageConfiguration> {
        return configurations.toMap()
    }
    
    /**
     * Export configuration to JSON
     */
    fun exportConfigurationToJson(language: SupportedLanguage): String? {
        val config = configurations[language] ?: return null
        return json.encodeToString(LanguageConfiguration.serializer(), config)
    }
}
