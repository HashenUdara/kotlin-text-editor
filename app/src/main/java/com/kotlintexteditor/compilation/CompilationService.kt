package com.kotlintexteditor.compilation

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.util.*

/**
 * Service responsible for compiling Kotlin code using ADB connection to desktop compiler
 */
class CompilationService private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Compilation status flow
    private val _compilationStatus = MutableStateFlow<CompilationStatus>(CompilationStatus.Idle)
    val compilationStatus: StateFlow<CompilationStatus> = _compilationStatus.asStateFlow()
    
    // Compilation results flow
    private val _compilationResult = MutableStateFlow<CompilationResult?>(null)
    val compilationResult: StateFlow<CompilationResult?> = _compilationResult.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: CompilationService? = null
        
        fun getInstance(context: Context): CompilationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CompilationService(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // ADB configuration
        private const val ADB_TEMP_DIR = "/sdcard/kotlin_compiler/"
        private const val SOURCE_FILE_NAME = "temp_source.kt"
        private const val RESULT_FILE_NAME = "compilation_result.json"
        private const val COMPILATION_TIMEOUT = 30_000L // 30 seconds
    }
    
    /**
     * Compile Kotlin source code using ADB connection
     */
    suspend fun compileKotlinCode(
        sourceCode: String,
        fileName: String = "Main.kt"
    ): CompilationResult = withContext(Dispatchers.IO) {
        try {
            _compilationStatus.value = CompilationStatus.Preparing
            
            // Check if ADB is available
            if (!isAdbAvailable()) {
                return@withContext CompilationResult.Error(
                    "ADB not found. Please ensure Android Debug Bridge is properly set up.",
                    emptyList()
                )
            }
            
            _compilationStatus.value = CompilationStatus.Connecting
            
            // Create temporary files for compilation
            val sourceFileResult = createTempSourceFile(sourceCode, fileName)
            if (!sourceFileResult) {
                return@withContext CompilationResult.Error(
                    "Failed to create temporary source file",
                    emptyList()
                )
            }
            
            _compilationStatus.value = CompilationStatus.Compiling
            
            // Execute compilation via ADB
            val compilationResult = executeCompilation(fileName)
            
            // Clean up temporary files
            cleanupTempFiles()
            
            _compilationStatus.value = CompilationStatus.Idle
            _compilationResult.value = compilationResult
            
            compilationResult
            
        } catch (e: Exception) {
            _compilationStatus.value = CompilationStatus.Idle
            val errorResult = CompilationResult.Error(
                "Compilation failed: ${e.message}",
                listOf(CompilationError(0, 0, "Internal error", e.message ?: "Unknown error"))
            )
            _compilationResult.value = errorResult
            errorResult
        }
    }
    
    /**
     * Check if ADB is available on the system
     */
    private suspend fun isAdbAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("adb", "version").start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create temporary source file via ADB
     */
    private suspend fun createTempSourceFile(sourceCode: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create temp directory on device
            executeAdbCommand("shell", "mkdir", "-p", ADB_TEMP_DIR)
            
            // Write source code to temp file
            val tempFile = File.createTempFile("kotlin_source", ".kt")
            tempFile.writeText(sourceCode)
            
            // Push file to device
            val pushResult = executeAdbCommand("push", tempFile.absolutePath, "$ADB_TEMP_DIR$SOURCE_FILE_NAME")
            
            // Clean up local temp file
            tempFile.delete()
            
            pushResult.isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Execute compilation via ADB shell command
     */
    private suspend fun executeCompilation(fileName: String): CompilationResult = withContext(Dispatchers.IO) {
        try {
            // Execute compilation script on device
            // This assumes a companion desktop script is monitoring the temp directory
            val compileCommand = arrayOf(
                "shell", 
                "echo", 
                "'{\"action\":\"compile\",\"file\":\"$SOURCE_FILE_NAME\",\"originalName\":\"$fileName\"}'", 
                ">", 
                "$ADB_TEMP_DIR/compile_request.json"
            )
            
            executeAdbCommand(*compileCommand)
            
            // Wait for compilation result (polling approach)
            var attempts = 0
            val maxAttempts = 30 // 30 seconds with 1-second intervals
            
            while (attempts < maxAttempts) {
                delay(1000) // Wait 1 second
                
                val resultExists = checkFileExists("$ADB_TEMP_DIR$RESULT_FILE_NAME")
                if (resultExists) {
                    return@withContext parseCompilationResult()
                }
                
                attempts++
            }
            
            // Timeout
            CompilationResult.Error(
                "Compilation timeout. Please ensure the desktop compiler service is running.",
                emptyList()
            )
            
        } catch (e: Exception) {
            CompilationResult.Error(
                "Compilation execution failed: ${e.message}",
                emptyList()
            )
        }
    }
    
    /**
     * Check if a file exists on the device
     */
    private suspend fun checkFileExists(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = executeAdbCommand("shell", "test", "-f", filePath)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parse compilation result from the device
     */
    private suspend fun parseCompilationResult(): CompilationResult = withContext(Dispatchers.IO) {
        try {
            // Pull result file from device
            val tempResultFile = File.createTempFile("compilation_result", ".json")
            val pullResult = executeAdbCommand("pull", "$ADB_TEMP_DIR$RESULT_FILE_NAME", tempResultFile.absolutePath)
            
            if (!pullResult.isSuccess) {
                return@withContext CompilationResult.Error("Failed to retrieve compilation result", emptyList())
            }
            
            // Parse JSON result
            val resultJson = tempResultFile.readText()
            tempResultFile.delete()
            
            // Simple JSON parsing (could be enhanced with a proper JSON library)
            parseJsonResult(resultJson)
            
        } catch (e: Exception) {
            CompilationResult.Error("Failed to parse compilation result: ${e.message}", emptyList())
        }
    }
    
    /**
     * Simple JSON parsing for compilation results
     */
    private fun parseJsonResult(json: String): CompilationResult {
        try {
            // Basic JSON parsing - this is simplified and could be enhanced
            if (json.contains("\"success\":true")) {
                val outputPattern = "\"output\":\"([^\"]*)\""
                val outputMatch = outputPattern.toRegex().find(json)
                val output = outputMatch?.groupValues?.get(1)?.replace("\\n", "\n") ?: ""
                
                return CompilationResult.Success(
                    message = "Compilation successful",
                    output = output,
                    bytecodeSize = output.length
                )
            } else {
                // Parse errors
                val errors = mutableListOf<CompilationError>()
                
                // Extract error messages (simplified)
                val errorPattern = "\"error\":\"([^\"]*)\""
                val errorMatch = errorPattern.toRegex().find(json)
                val errorMessage = errorMatch?.groupValues?.get(1)?.replace("\\n", "\n") ?: "Unknown compilation error"
                
                errors.add(CompilationError(1, 1, "Compilation Error", errorMessage))
                
                return CompilationResult.Error(
                    message = "Compilation failed",
                    errors = errors
                )
            }
        } catch (e: Exception) {
            return CompilationResult.Error(
                "Failed to parse compilation result",
                listOf(CompilationError(0, 0, "Parse Error", e.message ?: "Unknown parsing error"))
            )
        }
    }
    
    /**
     * Execute ADB command
     */
    private suspend fun executeAdbCommand(vararg command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            val fullCommand = arrayOf("adb") + command
            val process = ProcessBuilder(*fullCommand).start()
            
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            CommandResult(exitCode == 0, output, error)
        } catch (e: Exception) {
            CommandResult(false, "", e.message ?: "Command execution failed")
        }
    }
    
    /**
     * Clean up temporary files
     */
    private suspend fun cleanupTempFiles() = withContext(Dispatchers.IO) {
        try {
            executeAdbCommand("shell", "rm", "-rf", ADB_TEMP_DIR)
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

/**
 * Compilation status enum
 */
enum class CompilationStatus {
    Idle,
    Preparing,
    Connecting,
    Compiling
}

/**
 * Compilation result sealed class
 */
sealed class CompilationResult {
    data class Success(
        val message: String,
        val output: String,
        val bytecodeSize: Int
    ) : CompilationResult()
    
    data class Error(
        val message: String,
        val errors: List<CompilationError>
    ) : CompilationResult()
}

/**
 * Compilation error data class
 */
data class CompilationError(
    val line: Int,
    val column: Int,
    val type: String,
    val message: String
)

/**
 * Command execution result
 */
private data class CommandResult(
    val isSuccess: Boolean,
    val output: String,
    val error: String
)
