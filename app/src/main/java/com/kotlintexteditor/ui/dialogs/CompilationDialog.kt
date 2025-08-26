package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlintexteditor.compiler.CompilationResult
import com.kotlintexteditor.compiler.CompilationState
import com.kotlintexteditor.compiler.isInProgress
import com.kotlintexteditor.compiler.isCompleted

/**
 * Dialog for showing compilation progress and results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompilationDialog(
    isVisible: Boolean,
    compilationState: CompilationState,
    compilationResult: CompilationResult?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onTestConnection: () -> Unit
) {
    if (!isVisible) return
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !compilationState.isInProgress(),
            dismissOnClickOutside = !compilationState.isInProgress()
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                CompilationDialogHeader(
                    state = compilationState,
                    onDismiss = if (!compilationState.isInProgress()) onDismiss else null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on state
                when (compilationState) {
                    CompilationState.IDLE -> {
                        // Should not happen when dialog is visible
                    }
                    
                    CompilationState.TESTING_CONNECTION -> {
                        CompilationProgressContent(
                            message = "Testing ADB connection and desktop bridge...",
                            showProgress = true
                        )
                    }
                    
                    CompilationState.COMPILING -> {
                        CompilationProgressContent(
                            message = "Compiling source code...",
                            showProgress = true
                        )
                    }
                    
                    CompilationState.SUCCESS -> {
                        compilationResult?.let { result ->
                            if (result is CompilationResult.Success) {
                                CompilationSuccessContent(result = result)
                            }
                        }
                    }
                    
                    CompilationState.ERROR -> {
                        compilationResult?.let { result ->
                            if (result is CompilationResult.Error) {
                                CompilationErrorContent(result = result)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                CompilationDialogActions(
                    state = compilationState,
                    result = compilationResult,
                    onDismiss = onDismiss,
                    onRetry = onRetry,
                    onTestConnection = onTestConnection
                )
            }
        }
    }
}

@Composable
private fun CompilationDialogHeader(
    state: CompilationState,
    onDismiss: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            val (icon, iconColor) = when (state) {
                CompilationState.IDLE -> Icons.Default.Code to MaterialTheme.colorScheme.primary
                CompilationState.TESTING_CONNECTION -> Icons.Default.Wifi to MaterialTheme.colorScheme.primary
                CompilationState.COMPILING -> Icons.Default.Build to MaterialTheme.colorScheme.primary
                CompilationState.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                CompilationState.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Code Compilation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Close button (only when not in progress)
        if (onDismiss != null) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
private fun CompilationProgressContent(
    message: String,
    showProgress: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompilationSuccessContent(result: CompilationResult.Success) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Success message
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Compilation successful!",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compilation details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (result.outputPath.isNotEmpty()) {
                    CompilationDetailRow(
                        label = "Output:",
                        value = result.outputPath
                    )
                }
                
                CompilationDetailRow(
                    label = "Time:",
                    value = "${result.compilationTime}ms"
                )
                
                if (result.stdout.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CompilationDetailRow(
                        label = "Output:",
                        value = result.stdout,
                        isCode = true
                    )
                }
                
                if (result.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CompilationDetailRow(
                        label = "Warnings:",
                        value = result.warnings.joinToString("\n"),
                        isCode = true
                    )
                }
            }
        }
    }
}

@Composable
private fun CompilationErrorContent(result: CompilationResult.Error) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Error message
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = result.message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 200.dp)
            ) {
                if (result.details.isNotEmpty()) {
                    CompilationDetailRow(
                        label = "Details:",
                        value = result.details,
                        isCode = true
                    )
                }
                
                if (result.stdout.isNotEmpty()) {
                    if (result.details.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    CompilationDetailRow(
                        label = "Output:",
                        value = result.stdout,
                        isCode = true
                    )
                }
                
                if (result.errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CompilationDetailRow(
                        label = "Errors:",
                        value = result.errors.joinToString("\n"),
                        isCode = true
                    )
                }
            }
        }
    }
}

@Composable
private fun CompilationDetailRow(
    label: String,
    value: String,
    isCode: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompilationDialogActions(
    state: CompilationState,
    result: CompilationResult?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onTestConnection: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        when (state) {
            CompilationState.IDLE,
            CompilationState.TESTING_CONNECTION,
            CompilationState.COMPILING -> {
                // No actions during progress
            }
            
            CompilationState.SUCCESS -> {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
            
            CompilationState.ERROR -> {
                // Show different actions based on error type
                val isConnectionError = result is CompilationResult.Error && 
                        result.message.contains("bridge", ignoreCase = true)
                
                if (isConnectionError) {
                    TextButton(onClick = onTestConnection) {
                        Text("Test Connection")
                    }
                }
                
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
