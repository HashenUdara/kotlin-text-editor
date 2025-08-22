package com.kotlintexteditor.ui.components

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
import com.kotlintexteditor.compilation.CompilationResult
import com.kotlintexteditor.compilation.CompilationStatus

/**
 * Dialog to show compilation status and results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompilationStatusDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    compilationStatus: CompilationStatus,
    compilationResult: CompilationResult?,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            when (compilationStatus) {
                CompilationStatus.Idle -> Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                CompilationStatus.Preparing -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                CompilationStatus.Connecting -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                CompilationStatus.Compiling -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        title = {
            Text(
                text = when (compilationStatus) {
                    CompilationStatus.Idle -> "Compilation Result"
                    CompilationStatus.Preparing -> "Preparing..."
                    CompilationStatus.Connecting -> "Connecting to ADB..."
                    CompilationStatus.Compiling -> "Compiling..."
                },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status description
                when (compilationStatus) {
                    CompilationStatus.Preparing -> {
                        StatusCard(
                            icon = Icons.Default.Settings,
                            title = "Preparing Compilation",
                            description = "Setting up temporary files and checking ADB connection...",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    CompilationStatus.Connecting -> {
                        StatusCard(
                            icon = Icons.Default.Cable,
                            title = "Connecting to ADB",
                            description = "Establishing connection with desktop compiler bridge...",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    CompilationStatus.Compiling -> {
                        StatusCard(
                            icon = Icons.Default.Build,
                            title = "Compiling Code",
                            description = "Running Kotlin compiler on your code...",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    CompilationStatus.Idle -> {
                        // Show compilation result
                        compilationResult?.let { result ->
                            when (result) {
                                is CompilationResult.Success -> {
                                    CompilationSuccessContent(result)
                                }
                                is CompilationResult.Error -> {
                                    CompilationErrorContent(result)
                                }
                            }
                        } ?: run {
                            Text(
                                "Ready to compile Kotlin code via ADB connection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // ADB Setup Instructions (shown when idle and no result)
                if (compilationStatus == CompilationStatus.Idle && compilationResult == null) {
                    AdbSetupInstructions()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    when (compilationStatus) {
                        CompilationStatus.Idle -> "Close"
                        else -> "Cancel"
                    }
                )
            }
        }
    )
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompilationSuccessContent(result: CompilationResult.Success) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCard(
            icon = Icons.Default.CheckCircle,
            title = "Compilation Successful!",
            description = result.message,
            color = Color(0xFF4CAF50)
        )
        
        // Compilation details
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Compilation Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                CompilationDetailRow("Bytecode Size", "${result.bytecodeSize} bytes")
                
                if (result.output.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Compiler Output:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = result.output,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompilationErrorContent(result: CompilationResult.Error) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCard(
            icon = Icons.Default.Error,
            title = "Compilation Failed",
            description = result.message,
            color = MaterialTheme.colorScheme.error
        )
        
        // Error details
        if (result.errors.isNotEmpty()) {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Compilation Errors",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    result.errors.forEach { error ->
                        CompilationErrorItem(error)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompilationErrorItem(error: com.kotlintexteditor.compilation.CompilationError) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${error.type} (Line ${error.line}, Column ${error.column})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun CompilationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdbSetupInstructions() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "ADB Setup Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "To compile Kotlin code, you need to:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("1. Enable USB debugging on this device", style = MaterialTheme.typography.bodySmall)
                Text("2. Connect to a computer with ADB and Kotlin compiler", style = MaterialTheme.typography.bodySmall)
                Text("3. Run the desktop compiler bridge script", style = MaterialTheme.typography.bodySmall)
                Text("4. Try compiling your Kotlin code!", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
