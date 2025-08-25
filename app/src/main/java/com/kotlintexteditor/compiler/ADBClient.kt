package com.kotlintexteditor.compiler

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Handles ADB communication with desktop for compilation
 */
class ADBClient(private val context: Context) {
    
    companion object {
        private const val TAG = "ADBClient"
        private const val BRIDGE_PORT = 8766  // Android connects to this port (forwarded to desktop 8765)
        private const val CONNECTION_TIMEOUT = 10000L // 10 seconds
        private const val COMPILATION_TIMEOUT = 30000L // 30 seconds
        
        // Desktop bridge service commands
        private const val CMD_PING = "PING"
        private const val CMD_COMPILE = "COMPILE"
        private const val CMD_STATUS = "STATUS"
    }
    
    /**
     * Sends source file to desktop via direct socket connection
     */
    suspend fun sendSourceFile(fileName: String, sourceCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending source file: $fileName (${sourceCode.length} chars)")
                
                // Send source code directly via socket
                val command = "SEND_SOURCE $fileName ${sourceCode.length}\n$sourceCode"
                val result = sendBridgeCommand(command)
                
                if (!result.success) {
                    Log.e(TAG, "Failed to send source file: ${result.error}")
                    return@withContext false
                }
                
                Log.d(TAG, "Successfully sent file: $fileName")
                return@withContext true
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending source file", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Requests compilation from desktop bridge
     */
    suspend fun requestCompilation(compilationId: String, fileName: String): CompilationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Requesting compilation for: $fileName")
                
                // Send compilation request
                val compileCommand = "$CMD_COMPILE $compilationId $fileName"
                val response = sendBridgeCommand(compileCommand)
                
                if (!response.success) {
                    return@withContext CompilationResult.Error(
                        message = "Failed to start compilation",
                        details = response.error ?: "Unknown error"
                    )
                }
                
                // Wait for compilation result with timeout
                val result = withTimeoutOrNull(COMPILATION_TIMEOUT) {
                    pollCompilationStatus(compilationId)
                }
                
                return@withContext result ?: CompilationResult.Error(
                    message = "Compilation timeout",
                    details = "Compilation took longer than ${COMPILATION_TIMEOUT / 1000} seconds"
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during compilation request", e)
                return@withContext CompilationResult.Error(
                    message = "Compilation request failed",
                    details = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    /**
     * Checks if desktop bridge service is reachable
     */
    suspend fun checkConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = withTimeoutOrNull(CONNECTION_TIMEOUT) {
                    sendBridgeCommand(CMD_PING)
                }
                
                val connected = response?.success == true && response.output?.contains("PONG") == true
                Log.d(TAG, "Bridge connection check: $connected")
                return@withContext connected
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking bridge connection", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Test ADB connection with detailed logging - for debugging purposes
     */
    suspend fun testADBConnection(): String {
        return withContext(Dispatchers.IO) {
            val results = StringBuilder()
            
            try {
                results.append("=== ADB Connection Test ===\n")
                results.append("Target: localhost:$BRIDGE_PORT\n")
                results.append("Timeout: ${CONNECTION_TIMEOUT}ms\n\n")
                
                // Test 1: Basic socket connection
                results.append("1. Testing socket connection...\n")
                try {
                    val socket = java.net.Socket()
                    val startTime = System.currentTimeMillis()
                    socket.connect(java.net.InetSocketAddress("192.168.221.1", BRIDGE_PORT), 5000)
                    val connectTime = System.currentTimeMillis() - startTime
                    results.append("   ✓ Socket connected in ${connectTime}ms\n")
                    socket.close()
                } catch (e: Exception) {
                    results.append("   ✗ Socket connection failed: ${e.message}\n")
                    results.append("   Error type: ${e.javaClass.simpleName}\n")
                    return@withContext results.toString()
                }
                
                // Test 2: PING command
                results.append("\n2. Testing PING command...\n")
                try {
                    val startTime = System.currentTimeMillis()
                    val response = sendBridgeCommand("PING")
                    val responseTime = System.currentTimeMillis() - startTime
                    
                    results.append("   Response time: ${responseTime}ms\n")
                    results.append("   Success: ${response.success}\n")
                    results.append("   Output: '${response.output}'\n")
                    results.append("   Error: '${response.error}'\n")
                    
                    if (response.success && response.output == "PONG") {
                        results.append("   ✓ PING test successful\n")
                    } else {
                        results.append("   ✗ PING test failed\n")
                    }
                } catch (e: Exception) {
                    results.append("   ✗ PING command failed: ${e.message}\n")
                    results.append("   Error type: ${e.javaClass.simpleName}\n")
                }
                
                // Test 3: Test source sending
                results.append("\n3. Testing source file sending...\n")
                try {
                    val testCode = "fun main() { println(\"Test\") }"
                    val startTime = System.currentTimeMillis()
                    val success = sendSourceFile("test.kt", testCode)
                    val sendTime = System.currentTimeMillis() - startTime
                    
                    results.append("   Send time: ${sendTime}ms\n")
                    results.append("   Success: $success\n")
                    
                    if (success) {
                        results.append("   ✓ Source sending successful\n")
                    } else {
                        results.append("   ✗ Source sending failed\n")
                    }
                } catch (e: Exception) {
                    results.append("   ✗ Source sending failed: ${e.message}\n")
                    results.append("   Error type: ${e.javaClass.simpleName}\n")
                }
                
                results.append("\n=== Test Complete ===")
                return@withContext results.toString()
                
            } catch (e: Exception) {
                results.append("\nFatal test error: ${e.message}\n")
                results.append("Error type: ${e.javaClass.simpleName}\n")
                return@withContext results.toString()
            }
        }
    }
    
    /**
     * Polls compilation status until complete
     */
    private suspend fun pollCompilationStatus(compilationId: String): CompilationResult {
        return withContext(Dispatchers.IO) {
            var attempts = 0
            val maxAttempts = 30 // 30 seconds with 1 second intervals
            
            while (attempts < maxAttempts) {
                try {
                    val statusResponse = sendBridgeCommand("$CMD_STATUS $compilationId")
                    
                    if (statusResponse.success && statusResponse.output != null) {
                        val status = parseCompilationStatus(statusResponse.output)
                        
                        when (status.state) {
                            "COMPLETED" -> {
                                return@withContext CompilationResult.Success(
                                    outputPath = status.outputPath ?: "",
                                    compilationTime = status.compilationTime ?: 0L,
                                    warnings = status.warnings
                                )
                            }
                            "FAILED" -> {
                                return@withContext CompilationResult.Error(
                                    message = status.errorMessage ?: "Compilation failed",
                                    details = status.errorDetails ?: "",
                                    errors = status.errors
                                )
                            }
                            "RUNNING" -> {
                                // Continue polling
                                kotlinx.coroutines.delay(1000)
                                attempts++
                            }
                            else -> {
                                // Unknown state, continue polling
                                kotlinx.coroutines.delay(1000)
                                attempts++
                            }
                        }
                    } else {
                        kotlinx.coroutines.delay(1000)
                        attempts++
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error polling compilation status", e)
                    kotlinx.coroutines.delay(1000)
                    attempts++
                }
            }
            
            // Timeout reached
            return@withContext CompilationResult.Error(
                message = "Compilation status polling timeout",
                details = "Could not get final compilation status"
            )
        }
    }
    

    
    /**
     * Sends command to desktop bridge service via direct socket connection
     */
    private suspend fun sendBridgeCommand(command: String): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Connecting to bridge at localhost:$BRIDGE_PORT")
                
                // Connect directly to the bridge service via ADB port forwarding
                // Note: ADB port forwarding must be set up externally: adb forward tcp:8765 tcp:8765
                val socket = java.net.Socket()
                socket.connect(java.net.InetSocketAddress("192.168.221.1", BRIDGE_PORT), 5000)
                
                socket.use { sock ->
                    // Send command
                    val output = java.io.OutputStreamWriter(sock.getOutputStream())
                    output.write(command + "\n")
                    output.flush()
                    
                    // Read response
                    val input = java.io.BufferedReader(java.io.InputStreamReader(sock.getInputStream()))
                    val response = input.readLine() ?: ""
                    
                    Log.d(TAG, "Bridge command '$command' -> '$response'")
                    
                    return@withContext CommandResult(
                        success = !response.startsWith("ERROR"),
                        output = response,
                        error = if (response.startsWith("ERROR")) response else null
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to bridge service: $command", e)
                return@withContext CommandResult(
                    success = false,
                    error = "Connection failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Parses compilation status response
     */
    private fun parseCompilationStatus(response: String): CompilationStatus {
        try {
            // Expected format: STATE|output_path|compilation_time|messages
            val parts = response.split("|")
            
            return CompilationStatus(
                state = parts.getOrNull(0) ?: "UNKNOWN",
                outputPath = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
                compilationTime = parts.getOrNull(2)?.toLongOrNull(),
                errorMessage = if (parts.getOrNull(0) == "FAILED") parts.getOrNull(3) else null,
                errorDetails = if (parts.getOrNull(0) == "FAILED") parts.getOrNull(4) else null,
                warnings = emptyList(), // TODO: Parse warnings from response
                errors = emptyList() // TODO: Parse errors from response
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing compilation status: $response", e)
            return CompilationStatus(
                state = "UNKNOWN",
                errorMessage = "Failed to parse status response",
                errorDetails = response
            )
        }
    }
}

/**
 * Result of executing a command
 */
data class CommandResult(
    val success: Boolean,
    val output: String? = null,
    val error: String? = null
)

/**
 * Compilation status from desktop bridge
 */
data class CompilationStatus(
    val state: String,
    val outputPath: String? = null,
    val compilationTime: Long? = null,
    val errorMessage: String? = null,
    val errorDetails: String? = null,
    val warnings: List<CompilationMessage> = emptyList(),
    val errors: List<CompilationMessage> = emptyList()
)

