package com.kotlintexteditor.compiler

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Manages compilation process and state
 */
class CompilerManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CompilerManager"
    }
    
    private val adbClient = ADBClient(context)
    
    // Compilation state
    private val _compilationState = MutableStateFlow(CompilationState.IDLE)
    val compilationState: StateFlow<CompilationState> = _compilationState.asStateFlow()
    
    // Compilation result
    private val _compilationResult = MutableStateFlow<CompilationResult?>(null)
    val compilationResult: StateFlow<CompilationResult?> = _compilationResult.asStateFlow()
    
    // Bridge connection state
    private val _isBridgeConnected = MutableStateFlow(false)
    val isBridgeConnected: StateFlow<Boolean> = _isBridgeConnected.asStateFlow()
    
    // Run result
    private val _runResult = MutableStateFlow<RunResult?>(null)
    val runResult: StateFlow<RunResult?> = _runResult.asStateFlow()
    
    /**
     * Test ADB connection and desktop bridge
     */
    suspend fun testADBConnection(): String {
        return try {
            Log.d(TAG, "Testing ADB connection...")
            _compilationState.value = CompilationState.TESTING_CONNECTION
            
            val testResult = adbClient.testConnection()
            _isBridgeConnected.value = testResult.bridgeConnected
            
            _compilationState.value = CompilationState.IDLE
            
            testResult.getFormattedResults()
            
        } catch (e: Exception) {
            Log.e(TAG, "ADB test error", e)
            _compilationState.value = CompilationState.IDLE
            _isBridgeConnected.value = false
            
            "ADB Connection Test Failed:\n${e.message ?: "Unknown error"}"
        }
    }
    
    /**
     * Compile the given source code
     */
    suspend fun compileCode(filename: String, sourceCode: String): CompilationResult {
        return try {
            Log.d(TAG, "Starting compilation: $filename")
            
            // Validate inputs
            if (filename.isBlank()) {
                return CompilationResult.Error(
                    message = "Invalid filename",
                    details = "Filename cannot be empty"
                )
            }
            
            if (sourceCode.isBlank()) {
                return CompilationResult.Error(
                    message = "No source code",
                    details = "Source code cannot be empty"
                )
            }
            
            // Check file extension
            val extension = File(filename).extension.lowercase()
            if (extension !in listOf("kt", "java")) {
                return CompilationResult.Error(
                    message = "Unsupported file type",
                    details = "Only .kt and .java files are supported"
                )
            }
            
            // Update state
            _compilationState.value = CompilationState.COMPILING
            _compilationResult.value = null
            
            // Check bridge connection first
            Log.d(TAG, "Checking bridge connection...")
            val bridgeConnected = adbClient.checkBridgeConnection()
            _isBridgeConnected.value = bridgeConnected
            
            if (!bridgeConnected) {
                val errorResult = CompilationResult.Error(
                    message = "Desktop bridge not connected",
                    details = "Please make sure the desktop compiler bridge is running.\n" +
                            "Run start-bridge.bat on your computer and ensure your device is connected via USB."
                )
                _compilationResult.value = errorResult
                _compilationState.value = CompilationState.ERROR
                return errorResult
            }
            
            // Perform compilation
            Log.d(TAG, "Sending source code to desktop...")
            val result = adbClient.compileSource(filename, sourceCode)
            
            // Update state based on result
            when (result) {
                is CompilationResult.Success -> {
                    Log.d(TAG, "Compilation successful: ${result.outputPath}")
                    _compilationState.value = CompilationState.SUCCESS
                }
                is CompilationResult.Error -> {
                    Log.e(TAG, "Compilation failed: ${result.message}")
                    _compilationState.value = CompilationState.ERROR
                }
            }
            
            _compilationResult.value = result
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "Compilation error", e)
            
            val errorResult = CompilationResult.Error(
                message = "Compilation failed",
                details = e.message ?: "Unknown error occurred during compilation"
            )
            
            _compilationResult.value = errorResult
            _compilationState.value = CompilationState.ERROR
            
            return errorResult
        }
    }
    
    /**
     * Check if desktop bridge is connected
     */
    suspend fun checkBridgeConnection(): Boolean {
        return try {
            val connected = adbClient.checkBridgeConnection()
            _isBridgeConnected.value = connected
            connected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking bridge connection", e)
            _isBridgeConnected.value = false
            false
        }
    }
    
    /**
     * Run the compiled JAR file
     */
    suspend fun runCompiledCode(jarPath: String): RunResult {
        return try {
            Log.d(TAG, "Starting program execution: $jarPath")
            
            // Validate JAR path
            if (jarPath.isBlank()) {
                return RunResult.Error(
                    message = "Invalid JAR path",
                    details = "JAR path cannot be empty"
                )
            }
            
            // Update state
            _compilationState.value = CompilationState.COMPILING // Reuse for running
            _runResult.value = null
            
            // Check bridge connection first
            Log.d(TAG, "Checking bridge connection...")
            val bridgeConnected = adbClient.checkBridgeConnection()
            _isBridgeConnected.value = bridgeConnected
            
            if (!bridgeConnected) {
                _compilationState.value = CompilationState.ERROR
                return RunResult.Error(
                    message = "Desktop bridge not connected",
                    details = "Make sure the desktop bridge is running and device is connected via USB"
                )
            }
            
            // Execute the JAR
            Log.d(TAG, "Executing JAR file...")
            val result = adbClient.runJarFile(jarPath)
            _runResult.value = result
            
            // Log the run result details
            when (result) {
                is RunResult.Success -> {
                    Log.d(TAG, "Run successful - stdout: '${result.stdout}', stderr: '${result.stderr}', exitCode: ${result.exitCode}, time: ${result.executionTime}ms")
                }
                is RunResult.Error -> {
                    Log.e(TAG, "Run failed - message: '${result.message}', details: '${result.details}'")
                }
            }
            
            // Update state based on result
            _compilationState.value = when (result) {
                is RunResult.Success -> CompilationState.SUCCESS
                is RunResult.Error -> CompilationState.ERROR
            }
            
            Log.d(TAG, "Program execution completed: ${result.javaClass.simpleName}")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Run error", e)
            _compilationState.value = CompilationState.ERROR
            _isBridgeConnected.value = false
            
            val errorResult = RunResult.Error(
                message = "Run failed",
                details = e.message ?: "Unknown error"
            )
            _runResult.value = errorResult
            errorResult
        }
    }

    /**
     * Reset compilation state
     */
    fun resetState() {
        _compilationState.value = CompilationState.IDLE
        _compilationResult.value = null
        _runResult.value = null
    }
    
    /**
     * Get the current compilation state
     */
    fun getCurrentState(): CompilationState = _compilationState.value
    
    /**
     * Get the current compilation result
     */
    fun getCurrentResult(): CompilationResult? = _compilationResult.value
    
    /**
     * Get bridge connection status
     */
    fun isBridgeConnected(): Boolean = _isBridgeConnected.value
}

/**
 * Compilation states
 */
enum class CompilationState {
    IDLE,                   // No compilation in progress
    TESTING_CONNECTION,     // Testing ADB/bridge connection
    COMPILING,             // Compilation in progress
    SUCCESS,               // Compilation completed successfully
    ERROR                  // Compilation failed
}

/**
 * Extension functions for CompilationState
 */
fun CompilationState.isInProgress(): Boolean {
    return this == CompilationState.TESTING_CONNECTION || this == CompilationState.COMPILING
}

fun CompilationState.isCompleted(): Boolean {
    return this == CompilationState.SUCCESS || this == CompilationState.ERROR
}

fun CompilationState.getDisplayText(): String {
    return when (this) {
        CompilationState.IDLE -> "Ready"
        CompilationState.TESTING_CONNECTION -> "Testing connection..."
        CompilationState.COMPILING -> "Compiling..."
        CompilationState.SUCCESS -> "Compilation successful"
        CompilationState.ERROR -> "Compilation failed"
    }
}
