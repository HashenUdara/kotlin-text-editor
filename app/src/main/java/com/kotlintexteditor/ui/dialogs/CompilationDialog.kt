package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kotlintexteditor.compiler.CompilationResult
import com.kotlintexteditor.compiler.CompilationState
import com.kotlintexteditor.compiler.MessageType

/**
 * Dialog showing compilation progress and results
 */
@Composable
fun CompilationDialog(
    isVisible: Boolean,
    compilationState: CompilationState,
    compilationResult: CompilationResult?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    CompilationDialogHeader(compilationState)
                    
                    // Content based on state
                    when (compilationState) {
                        CompilationState.IDLE -> {
                            // Should not show dialog in IDLE state
                        }
                        CompilationState.PREPARING,
                        CompilationState.SENDING_FILES,
                        CompilationState.COMPILING -> {
                            CompilationProgressContent(compilationState)
                        }
                        CompilationState.SUCCESS -> {
                            compilationResult?.let { result ->
                                if (result is CompilationResult.Success) {
                                    CompilationSuccessContent(result)
                                }
                            }
                        }
                        CompilationState.ERROR -> {
                            compilationResult?.let { result ->
                                if (result is CompilationResult.Error) {
                                    CompilationErrorContent(result, onRetry)
                                }
                            }
                        }
                    }
                    
                    // Actions
                    CompilationDialogActions(
                        compilationState = compilationState,
                        onDismiss = onDismiss,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun CompilationDialogHeader(compilationState: CompilationState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val (icon, title, color) = when (compilationState) {
            CompilationState.PREPARING,
            CompilationState.SENDING_FILES,
            CompilationState.COMPILING -> Triple(
                Icons.Default.Build,
                "Compiling Code",
                MaterialTheme.colorScheme.primary
            )
            CompilationState.SUCCESS -> Triple(
                Icons.Default.CheckCircle,
                "Compilation Successful",
                MaterialTheme.colorScheme.primary
            )
            CompilationState.ERROR -> Triple(
                Icons.Default.Error,
                "Compilation Failed",
                MaterialTheme.colorScheme.error
            )
            else -> Triple(
                Icons.Default.Build,
                "Compilation",
                MaterialTheme.colorScheme.onSurface
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CompilationProgressContent(compilationState: CompilationState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        val statusText = when (compilationState) {
            CompilationState.PREPARING -> "Preparing compilation..."
            CompilationState.SENDING_FILES -> "Sending files to desktop..."
            CompilationState.COMPILING -> "Compiling Kotlin code..."
            else -> "Processing..."
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "This may take a few moments...",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompilationSuccessContent(result: CompilationResult.Success) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Success message
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✅ Code compiled successfully!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (result.compilationTime > 0) {
                    Text(
                        text = "Compilation time: ${result.compilationTime}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (result.outputPath.isNotBlank()) {
                    Text(
                        text = "Output: ${result.outputPath}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Warnings (if any)
        if (result.warnings.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Warnings (${result.warnings.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    result.warnings.take(3).forEach { warning ->
                        Text(
                            text = "• ${warning.message}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    if (result.warnings.size > 3) {
                        Text(
                            text = "... and ${result.warnings.size - 3} more warnings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompilationErrorContent(
    result: CompilationResult.Error,
    onRetry: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Error message
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "❌ ${result.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                if (result.details.isNotBlank()) {
                    Text(
                        text = result.details,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Compilation errors (if any)
        if (result.errors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Compilation Errors:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    result.errors.forEach { error ->
                        CompilationMessageItem(error)
                    }
                }
            }
        }
        
        // Troubleshooting tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Troubleshooting:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val tips = listOf(
                    "Check that your desktop bridge service is running",
                    "Verify ADB connection with 'adb devices'",
                    "Ensure kotlinc is installed on desktop",
                    "Check code syntax for errors"
                )
                
                tips.forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CompilationMessageItem(message: com.kotlintexteditor.compiler.CompilationMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (icon, color) = when (message.type) {
            MessageType.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
            MessageType.WARNING -> Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
            MessageType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (message.line != null || message.file != null) {
                val location = buildString {
                    message.file?.let { append("$it") }
                    message.line?.let { 
                        if (isNotEmpty()) append(":")
                        append("line $it")
                    }
                    message.column?.let { append(":$it") }
                }
                
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompilationDialogActions(
    compilationState: CompilationState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        when (compilationState) {
            CompilationState.PREPARING,
            CompilationState.SENDING_FILES,
            CompilationState.COMPILING -> {
                // Show only cancel during compilation
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
            CompilationState.ERROR -> {
                // Show both retry and close for errors
                OutlinedButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
                
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
            else -> {
                // Show only close for success and idle
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

