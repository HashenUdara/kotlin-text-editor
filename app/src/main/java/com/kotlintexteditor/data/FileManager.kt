package com.kotlintexteditor.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class FileManager(private val context: Context) {
    
    data class FileResult(
        val success: Boolean,
        val content: String = "",
        val fileName: String = "",
        val uri: Uri? = null,
        val error: String? = null
    )
    
    /**
     * Read file content from URI
     */
    suspend fun readFile(uri: Uri): FileResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            
            if (inputStream == null) {
                return@withContext FileResult(
                    success = false,
                    error = "Could not open file for reading"
                )
            }
            
            val content = inputStream.bufferedReader().use { it.readText() }
            val fileName = getFileName(uri) ?: "Unknown"
            
            FileResult(
                success = true,
                content = content,
                fileName = fileName,
                uri = uri
            )
        } catch (e: Exception) {
            FileResult(
                success = false,
                error = "Error reading file: ${e.message}"
            )
        }
    }
    
    /**
     * Write content to file URI
     */
    suspend fun writeFile(uri: Uri, content: String): FileResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val outputStream = contentResolver.openOutputStream(uri, "wt")
            
            if (outputStream == null) {
                return@withContext FileResult(
                    success = false,
                    error = "Could not open file for writing"
                )
            }
            
            outputStream.bufferedWriter().use { writer ->
                writer.write(content)
            }
            
            val fileName = getFileName(uri) ?: "Unknown"
            
            FileResult(
                success = true,
                fileName = fileName,
                uri = uri
            )
        } catch (e: Exception) {
            FileResult(
                success = false,
                error = "Error writing file: ${e.message}"
            )
        }
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(uri: Uri): String? {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.name
        } catch (e: Exception) {
            // Fallback to last path segment
            uri.lastPathSegment?.substringAfterLast('/')
        }
    }
    
    /**
     * Get file extension from URI
     */
    fun getFileExtension(uri: Uri): String {
        val fileName = getFileName(uri) ?: return ""
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * Check if file exists and is readable
     */
    fun isFileReadable(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.exists() == true && documentFile.canRead()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if file is writable
     */
    fun isFileWritable(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.exists() == true && documentFile.canWrite()
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        // Supported file extensions
        val SUPPORTED_EXTENSIONS = listOf(
            "txt", "kt", "kts", "java", "py", "js", "ts", 
            "html", "css", "xml", "json", "md", "c", "cpp", "h"
        )
        
        // MIME types for file picking
        val TEXT_MIME_TYPES = arrayOf(
            "text/plain",
            "text/x-kotlin",
            "text/x-java-source",
            "application/javascript",
            "text/html",
            "text/css",
            "application/json",
            "text/markdown",
            "*/*" // Allow all files as fallback
        )
    }
}


