package com.kotlintexteditor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Text operations menu with copy, paste, cut, undo, redo buttons
 */
@Composable
fun TextOperationsToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    canPaste: Boolean,
    hasSelection: Boolean,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo
            TextOperationButton(
                icon = Icons.Default.Undo,
                label = "Undo",
                enabled = canUndo,
                onClick = onUndo
            )
            
            // Redo
            TextOperationButton(
                icon = Icons.Default.Redo,
                label = "Redo",
                enabled = canRedo,
                onClick = onRedo
            )
            
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )
            
            // Cut
            TextOperationButton(
                icon = Icons.Default.ContentCut,
                label = "Cut",
                enabled = hasSelection,
                onClick = onCut
            )
            
            // Copy
            TextOperationButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                enabled = hasSelection,
                onClick = onCopy
            )
            
            // Paste
            TextOperationButton(
                icon = Icons.Default.ContentPaste,
                label = "Paste",
                enabled = canPaste,
                onClick = onPaste
            )
            
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )
            
            // Select All
            TextOperationButton(
                icon = Icons.Default.SelectAll,
                label = "All",
                enabled = true,
                onClick = onSelectAll
            )
        }
    }
}

@Composable
private fun TextOperationButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}

/**
 * Floating Action Button for quick text operations
 */
@Composable
fun TextOperationsFAB(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Redo FAB (appears when expanded)
        if (expanded && canRedo) {
            SmallFloatingActionButton(
                onClick = {
                    onRedo()
                    expanded = false
                }
            ) {
                Icon(Icons.Default.Redo, contentDescription = "Redo")
            }
        }
        
        // Undo FAB (appears when expanded)
        if (expanded && canUndo) {
            SmallFloatingActionButton(
                onClick = {
                    onUndo()
                    expanded = false
                }
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo")
            }
        }
        
        // Main FAB
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Edit,
                contentDescription = if (expanded) "Close" else "Text Operations"
            )
        }
    }
}

/**
 * Context menu for text selection
 */
@Composable
fun TextSelectionContextMenu(
    hasSelection: Boolean,
    canPaste: Boolean,
    clipboardPreview: String?,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onSelectAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        if (hasSelection) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    onCopy()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                }
            )
            
            DropdownMenuItem(
                text = { Text("Cut") },
                onClick = {
                    onCut()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.ContentCut, contentDescription = null)
                }
            )
        }
        
        if (canPaste) {
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("Paste")
                        clipboardPreview?.let { preview ->
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                onClick = {
                    onPaste()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                }
            )
        }
        
        DropdownMenuItem(
            text = { Text("Select All") },
            onClick = {
                onSelectAll()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.SelectAll, contentDescription = null)
            }
        )
    }
}
