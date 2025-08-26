package com.kotlintexteditor.compiler

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.UUID

/**
 * ADB Client for communicating with desktop compiler bridge
 * Uses file-based communication via ADB push/pull
 */
class ADBClient(private val context: Context) {
    
    companion object {
        private const val TAG = "ADBClient"
        private const val CONNECTION_TIMEOUT = 10000L // 10 seconds
        private const val COMPILATION_TIMEOUT = 60000L // 60 seconds
        private const val POLL_INTERVAL = 1000L // 1 second
        
        // File names (paths will be determined at runtime)
        private const val COMMAND_FILE_NAME = "kotlin_editor_cmd.txt"
        private const val RESPONSE_FILE_NAME = "kotlin_editor_response.json"
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Get the command file path in external files directory
     */
    private fun getCommandFilePath(): String {
        val externalDir = context.getExternalFilesDir(null)
        return File(externalDir, COMMAND_FILE_NAME).absolutePath
    }
    
    /**
     * Get the response file path in external files directory
     */
    private fun getResponseFilePath(): String {
        val externalDir = context.getExternalFilesDir(null)
        return File(externalDir, RESPONSE_FILE_NAME).absolutePath
    }
    
    /**
     * Test ADB connection with desktop bridge
     */
    suspend fun testConnection(): ADBTestResult {
        return withContext(Dispatchers.IO) {
            val result = ADBTestResult()
            
            try {
                result.addStep("Testing device storage access...")
                result.addStep("External files directory: ${context.getExternalFilesDir(null)?.absolutePath}")
                
                // Test 1: Check if we can write to external files directory
                val externalDir = context.getExternalFilesDir(null)
                val testFile = File(externalDir, "kotlin_editor_test.txt")
                val testContent = "ADB test - ${System.currentTimeMillis()}"
                
                try {
                    testFile.writeText(testContent)
                    result.addSuccess("Device storage write test passed")
                } catch (e: Exception) {
                    result.addError("Cannot write to device storage: ${e.message}")
                    result.addError("Exception type: ${e.javaClass.simpleName}")
                    result.addError("Stack trace: ${e.stackTraceToString()}")
                    return@withContext result
                }
                
                // Test 2: Check if we can read back the file
                try {
                    val readContent = testFile.readText()
                    if (readContent.contains(testContent)) {
                        result.addSuccess("Device storage read test passed")
                    } else {
                        result.addError("Cannot read correct content from device storage")
                        return@withContext result
                    }
                } catch (e: Exception) {
                    result.addError("Cannot read from device storage: ${e.message}")
                    return@withContext result
                }
                
                // Test 3: Clean up test file
                try {
                    testFile.delete()
                    result.addSuccess("Device storage cleanup successful")
                } catch (e: Exception) {
                    result.addWarning("Could not clean up test file: ${e.message}")
                }
                
                // Test 4: Try to ping the desktop bridge
                result.addStep("Testing desktop bridge connection...")
                val pingSuccess = pingDesktopBridge()
                if (pingSuccess) {
                    result.addSuccess("Desktop bridge is responding")
                    result.bridgeConnected = true
                } else {
                    result.addWarning("Desktop bridge is not responding (bridge may not be started)")
                    result.addStep("Make sure:")
                    result.addStep("1. Desktop bridge is running (start-bridge.bat)")
                    result.addStep("2. Device is connected via USB")
                    result.addStep("3. USB debugging is enabled")
                }
                
                result.overallSuccess = true
                
            } catch (e: Exception) {
                result.addError("Connection test failed: ${e.message}")
                result.addError("Exception type: ${e.javaClass.simpleName}")
                result.addError("Full stack trace: ${e.stackTraceToString()}")
                Log.e(TAG, "Connection test error", e)
            }
            
            return@withContext result
        }
    }
    
    /**
     * Send source code to desktop for compilation
     */
    suspend fun compileSource(filename: String, sourceCode: String): CompilationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting compilation: $filename")
                
                // Create compilation command
                val command = CompileCommand(
                    type = "compile",
                    filename = filename,
                    source_code = sourceCode,
                    timestamp = System.currentTimeMillis()
                )
                
                // Send command to desktop
                val commandSent = sendCompileCommandToDesktop(command)
                if (!commandSent) {
                    return@withContext CompilationResult.Error(
                        message = "Failed to send compilation command to desktop",
                        details = "Could not write command file via ADB"
                    )
                }
                
                // Wait for response
                val response = waitForResponse(COMPILATION_TIMEOUT)
                if (response == null) {
                    return@withContext CompilationResult.Error(
                        message = "Compilation timeout",
                        details = "No response from desktop bridge within ${COMPILATION_TIMEOUT / 1000} seconds"
                    )
                }
                
                // Parse compilation result
                return@withContext parseCompilationResponse(response)
                
            } catch (e: Exception) {
                Log.e(TAG, "Compilation error", e)
                return@withContext CompilationResult.Error(
                    message = "Compilation failed",
                    details = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    /**
     * Run compiled JAR file on desktop
     */
    suspend fun runJarFile(jarPath: String): RunResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting JAR execution: $jarPath")
                
                // Create run command
                val command = RunCommand(
                    type = "run",
                    jar_path = jarPath,
                    timestamp = System.currentTimeMillis()
                )
                
                // Send command to desktop
                val commandSent = sendRunCommandToDesktop(command)
                if (!commandSent) {
                    return@withContext RunResult.Error(
                        message = "Failed to send run command to desktop",
                        details = "Could not write command file via ADB"
                    )
                }
                
                // Wait for response
                val response = waitForResponse(CONNECTION_TIMEOUT)
                if (response == null) {
                    return@withContext RunResult.Error(
                        message = "Run timeout",
                        details = "No response from desktop bridge within ${CONNECTION_TIMEOUT / 1000} seconds"
                    )
                }
                
                // Parse run result
                return@withContext parseRunResponse(response)
                
            } catch (e: Exception) {
                Log.e(TAG, "Run error", e)
                return@withContext RunResult.Error(
                    message = "Run failed",
                    details = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Check if desktop bridge is running
     */
    suspend fun checkBridgeConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            pingDesktopBridge()
        }
    }
    
    private suspend fun pingDesktopBridge(): Boolean {
        return try {
            val command = PingCommand(
                type = "ping",
                timestamp = System.currentTimeMillis()
            )
            
            val commandSent = sendPingCommandToDesktop(command)
            if (!commandSent) return false
            
            val response = waitForResponse(CONNECTION_TIMEOUT)
            response != null && response.contains("pong")
            
        } catch (e: Exception) {
            Log.e(TAG, "Ping error", e)
            false
        }
    }
    
    private suspend fun sendCompileCommandToDesktop(command: CompileCommand): Boolean {
        return try {
            val commandJson = json.encodeToString(command)
            
            // Log the JSON we're about to send
            Log.d(TAG, "Sending compile command JSON: $commandJson")
            Log.d(TAG, "JSON length: ${commandJson.length} characters")
            Log.d(TAG, "JSON bytes: ${commandJson.toByteArray().contentToString()}")
            
            // Write command to external files directory
            val commandFilePath = getCommandFilePath()
            val commandFile = File(commandFilePath)
            commandFile.writeText(commandJson)
            
            // Verify what was actually written
            val writtenContent = commandFile.readText()
            Log.d(TAG, "Verified written content: $writtenContent")
            Log.d(TAG, "Content matches: ${commandJson == writtenContent}")
            
            Log.d(TAG, "Compile command sent to desktop: $commandFilePath")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending compile command to desktop", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            return false
        }
    }
    
    private suspend fun sendPingCommandToDesktop(command: PingCommand): Boolean {
        return try {
            val commandJson = json.encodeToString(command)
            
            // Log the JSON we're about to send
            Log.d(TAG, "Sending ping command JSON: $commandJson")
            Log.d(TAG, "JSON length: ${commandJson.length} characters")
            Log.d(TAG, "JSON bytes: ${commandJson.toByteArray().contentToString()}")
            
            // Write command to external files directory
            val commandFilePath = getCommandFilePath()
            val commandFile = File(commandFilePath)
            commandFile.writeText(commandJson)
            
            // Verify what was actually written
            val writtenContent = commandFile.readText()
            Log.d(TAG, "Verified written content: $writtenContent")
            Log.d(TAG, "Content matches: ${commandJson == writtenContent}")
            
            Log.d(TAG, "Ping command sent to desktop: $commandFilePath")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending ping command to desktop", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            return false
        }
    }
    
    private suspend fun sendRunCommandToDesktop(command: RunCommand): Boolean {
        return try {
            val commandJson = json.encodeToString(command)
            
            // Log the JSON we're about to send
            Log.d(TAG, "Sending run command JSON: $commandJson")
            Log.d(TAG, "JSON length: ${commandJson.length} characters")
            Log.d(TAG, "JSON bytes: ${commandJson.toByteArray().contentToString()}")
            
            // Write command to external files directory
            val commandFilePath = getCommandFilePath()
            val commandFile = File(commandFilePath)
            commandFile.writeText(commandJson)
            
            // Verify what was actually written
            val writtenContent = commandFile.readText()
            Log.d(TAG, "Verified written content: $writtenContent")
            Log.d(TAG, "Content matches: ${commandJson == writtenContent}")
            
            Log.d(TAG, "Run command sent to desktop: $commandFilePath")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending run command to desktop", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            return false
        }
    }
    
    private suspend fun waitForResponse(timeoutMs: Long): String? {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                // Check if response file exists
                val responseFilePath = getResponseFilePath()
                val responseFile = File(responseFilePath)
                
                if (responseFile.exists()) {
                    // Response file exists, read it
                    val response = responseFile.readText()
                    
                    // Log the response we received
                    Log.d(TAG, "Received response from desktop: $response")
                    Log.d(TAG, "Response length: ${response.length} characters")
                    Log.d(TAG, "Response bytes: ${response.toByteArray().contentToString()}")
                    
                    // Clean up response file
                    responseFile.delete()
                    
                    Log.d(TAG, "Response file cleaned up")
                    return response
                }
                
                // Wait before next check
                delay(POLL_INTERVAL)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error waiting for response", e)
                delay(POLL_INTERVAL)
            }
        }
        
        Log.w(TAG, "Response timeout after ${timeoutMs}ms")
        return null
    }
    
    private fun parseCompilationResponse(response: String): CompilationResult {
        return try {
            Log.d(TAG, "Parsing compilation response: $response")
            
            // Clean up response - remove any extra whitespace or control characters
            val cleanResponse = response.trim()
            Log.d(TAG, "Cleaned response: $cleanResponse")
            
            // Try to parse as a generic map first
            val responseData = json.decodeFromString<Map<String, Any?>>(cleanResponse)
            Log.d(TAG, "Parsed response data: $responseData")
            
            val success = responseData["success"] as? Boolean ?: false
            Log.d(TAG, "Compilation success: $success")
            
            if (success) {
                val result = CompilationResult.Success(
                    outputPath = responseData["output_file"] as? String ?: "",
                    compilationTime = (responseData["compilation_time"] as? Double)?.toLong() ?: 0L,
                    stdout = responseData["stdout"] as? String ?: "",
                    stderr = responseData["stderr"] as? String ?: ""
                )
                Log.d(TAG, "Created success result: $result")
                result
            } else {
                val result = CompilationResult.Error(
                    message = responseData["error_message"] as? String ?: "Compilation failed",
                    details = responseData["stderr"] as? String ?: "",
                    stdout = responseData["stdout"] as? String ?: ""
                )
                Log.d(TAG, "Created error result: $result")
                result
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing compilation response", e)
            Log.e(TAG, "Raw response that failed parsing: '$response'")
            Log.e(TAG, "Response length: ${response.length}")
            Log.e(TAG, "Response as bytes: ${response.toByteArray().contentToString()}")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            
            // If JSON parsing failed but we can see it's a success, try to extract the output path manually
            if (response.contains("\"success\": true") && response.contains("output_file")) {
                Log.w(TAG, "JSON parsing failed but response indicates success - trying manual extraction")
                
                // Try to extract the output path using regex
                val outputPathPattern = "\"output_file\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val outputPathMatch = outputPathPattern.find(response)
                val extractedPath = outputPathMatch?.groupValues?.get(1)?.replace("\\\\", "\\") ?: ""
                
                Log.d(TAG, "Extracted output path: $extractedPath")
                
                CompilationResult.Success(
                    outputPath = extractedPath,
                    compilationTime = 0L,
                    stdout = "",
                    stderr = ""
                )
            } else {
                CompilationResult.Error(
                    message = "Failed to parse compilation response",
                    details = "Raw response: '$response'\nError: ${e.message}"
                )
            }
        }
    }
    
    private fun parseRunResponse(response: String): RunResult {
        return try {
            Log.d(TAG, "Parsing run response: $response")
            
            // Clean up response - remove any extra whitespace or control characters
            val cleanResponse = response.trim()
            Log.d(TAG, "Cleaned response: $cleanResponse")
            
            // Try to parse as a generic map first
            val responseData = json.decodeFromString<Map<String, Any?>>(cleanResponse)
            Log.d(TAG, "Parsed response data: $responseData")
            
            val success = responseData["success"] as? Boolean ?: false
            Log.d(TAG, "Run success: $success")
            
            if (success) {
                val result = RunResult.Success(
                    stdout = responseData["stdout"] as? String ?: "",
                    stderr = responseData["stderr"] as? String ?: "",
                    executionTime = (responseData["execution_time"] as? Double)?.toLong() ?: 0L,
                    exitCode = (responseData["exit_code"] as? Double)?.toInt() ?: 0,
                    jarPath = responseData["jar_path"] as? String ?: ""
                )
                Log.d(TAG, "Created run success result: $result")
                result
            } else {
                val result = RunResult.Error(
                    message = responseData["error_message"] as? String ?: "Program execution failed",
                    details = responseData["stderr"] as? String ?: "",
                    stdout = responseData["stdout"] as? String ?: ""
                )
                Log.d(TAG, "Created run error result: $result")
                result
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing run response", e)
            Log.e(TAG, "Raw response that failed parsing: '$response'")
            Log.e(TAG, "Response length: ${response.length}")
            Log.e(TAG, "Response as bytes: ${response.toByteArray().contentToString()}")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            
            // If JSON parsing failed but we can see it's a success, create a success result manually
            if (response.contains("\"success\": true") && response.contains("stdout")) {
                Log.w(TAG, "JSON parsing failed but response indicates success - creating manual success result")
                RunResult.Success(
                    stdout = "Program executed successfully (see logs for details)",
                    stderr = "",
                    executionTime = 0L,
                    exitCode = 0,
                    jarPath = ""
                )
            } else {
                RunResult.Error(
                    message = "Failed to parse run response",
                    details = "Raw response: '$response'\nError: ${e.message}"
                )
            }
        }
    }
    

}

// Data classes for communication
@Serializable
data class CompileCommand(
    val type: String,
    val filename: String,
    val source_code: String,
    val timestamp: Long
)

@Serializable
data class PingCommand(
    val type: String,
    val timestamp: Long
)

@Serializable
data class RunCommand(
    val type: String,
    val jar_path: String,
    val timestamp: Long
)

// Result classes

class ADBTestResult {
    var overallSuccess = false
    var bridgeConnected = false
    private val steps = mutableListOf<String>()
    
    fun addStep(step: String) {
        steps.add("• $step")
        Log.d("ADBClient", step)
    }
    
    fun addSuccess(message: String) {
        steps.add("✓ $message")
        Log.d("ADBClient", "SUCCESS: $message")
    }
    
    fun addWarning(message: String) {
        steps.add("⚠ $message")
        Log.w("ADBClient", "WARNING: $message")
    }
    
    fun addError(message: String) {
        steps.add("✗ $message")
        Log.e("ADBClient", "ERROR: $message")
    }
    
    fun getFormattedResults(): String {
        return buildString {
            appendLine("=== ADB Connection Test Results ===")
            appendLine()
            steps.forEach { step ->
                appendLine(step)
            }
            appendLine()
            appendLine("Overall Status: ${if (overallSuccess) "✓ PASSED" else "✗ FAILED"}")
            if (bridgeConnected) {
                appendLine("Bridge Status: ✓ CONNECTED")
            } else {
                appendLine("Bridge Status: ⚠ NOT CONNECTED")
            }
        }
    }
}

// Compilation result classes
sealed class CompilationResult {
    data class Success(
        val outputPath: String,
        val compilationTime: Long,
        val stdout: String = "",
        val stderr: String = "",
        val warnings: List<String> = emptyList()
    ) : CompilationResult()
    
    data class Error(
        val message: String,
        val details: String,
        val stdout: String = "",
        val errors: List<String> = emptyList()
    ) : CompilationResult()
}

// Run result classes
sealed class RunResult {
    data class Success(
        val stdout: String,
        val stderr: String,
        val executionTime: Long,
        val exitCode: Int,
        val jarPath: String
    ) : RunResult()
    
    data class Error(
        val message: String,
        val details: String,
        val stdout: String = ""
    ) : RunResult()
}