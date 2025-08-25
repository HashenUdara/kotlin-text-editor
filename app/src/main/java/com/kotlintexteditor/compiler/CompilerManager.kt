package com.kotlintexteditor.compiler

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages Kotlin compilation through ADB connection to desktop
 */
class CompilerManager(private val context: Context) {
    
    private val adbClient = ADBClient(context)
    
    private val _compilationState = MutableStateFlow(CompilationState.IDLE)
    val compilationState: StateFlow<CompilationState> = _compilationState.asStateFlow()
    
    private val _compilationResult = MutableStateFlow<CompilationResult?>(null)
    val compilationResult: StateFlow<CompilationResult?> = _compilationResult.asStateFlow()
    
    /**
     * Compiles Kotlin source code using desktop kotlinc
     */
    suspend fun compileKotlinCode(
        sourceCode: String,
        fileName: String = "Main.kt"
    ): CompilationResult {
        try {
            _compilationState.value = CompilationState.PREPARING
            
            // Generate unique compilation ID
            val compilationId = UUID.randomUUID().toString().take(8)
            val sourceFileName = "${compilationId}_$fileName"
            
            _compilationState.value = CompilationState.SENDING_FILES
            
            // Send source code to desktop
            val success = adbClient.sendSourceFile(sourceFileName, sourceCode)
            if (!success) {
                val errorResult = CompilationResult.Error(
                    message = "Failed to send source file to desktop",
                    details = "Check ADB connection and desktop bridge service"
                )
                _compilationResult.value = errorResult
                _compilationState.value = CompilationState.ERROR
                return errorResult
            }
            
            _compilationState.value = CompilationState.COMPILING
            
            // Request compilation
            val result = adbClient.requestCompilation(compilationId, sourceFileName)
            
            _compilationResult.value = result
            _compilationState.value = when (result) {
                is CompilationResult.Success -> CompilationState.SUCCESS
                is CompilationResult.Error -> CompilationState.ERROR
            }
            
            return result
            
        } catch (e: Exception) {
            val errorResult = CompilationResult.Error(
                message = "Compilation failed with exception",
                details = e.message ?: "Unknown error occurred"
            )
            _compilationResult.value = errorResult
            _compilationState.value = CompilationState.ERROR
            return errorResult
        }
    }
    
    /**
     * Checks if desktop bridge service is available
     */
    suspend fun checkBridgeConnection(): Boolean {
        return adbClient.checkConnection()
    }
    
    /**
     * Clears compilation state
     */
    fun clearCompilationState() {
        _compilationState.value = CompilationState.IDLE
        _compilationResult.value = null
    }
    
    /**
     * Test ADB connection for debugging purposes
     */
    suspend fun testADBConnection(): String {
        return adbClient.testADBConnection()
    }
}

/**
 * Represents the current state of compilation
 */
enum class CompilationState {
    IDLE,
    PREPARING,
    SENDING_FILES,
    COMPILING,
    SUCCESS,
    ERROR
}

/**
 * Represents compilation result
 */
sealed class CompilationResult {
    data class Success(
        val outputPath: String,
        val compilationTime: Long,
        val warnings: List<CompilationMessage> = emptyList()
    ) : CompilationResult()
    
    data class Error(
        val message: String,
        val details: String,
        val errors: List<CompilationMessage> = emptyList()
    ) : CompilationResult()
}

/**
 * Represents a compilation message (error, warning, info)
 */
data class CompilationMessage(
    val type: MessageType,
    val message: String,
    val line: Int? = null,
    val column: Int? = null,
    val file: String? = null
)

enum class MessageType {
    ERROR,
    WARNING,
    INFO
}

