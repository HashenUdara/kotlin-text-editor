package com.kotlintexteditor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Navigation drawer with organized menu items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    onFindReplaceClick: () -> Unit,
    onLanguageConfigClick: () -> Unit,
    onAutoSaveToggle: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTestADBClick: () -> Unit,
    isAutoSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Section
        DrawerHeader()
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Editor Tools Section
        DrawerSection(title = "Editor Tools") {
            DrawerMenuItem(
                icon = Icons.Default.Search,
                title = "Find & Replace",
                subtitle = "Search and replace text",
                onClick = onFindReplaceClick
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Settings,
                title = "Language Settings",
                subtitle = "Configure syntax highlighting",
                onClick = onLanguageConfigClick
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // File Operations Section
        DrawerSection(title = "File Operations") {
            DrawerMenuItem(
                icon = if (isAutoSaveEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff,
                title = "Auto-save",
                subtitle = if (isAutoSaveEnabled) "Currently enabled" else "Currently disabled",
                onClick = onAutoSaveToggle,
                trailing = {
                    Switch(
                        checked = isAutoSaveEnabled,
                        onCheckedChange = { onAutoSaveToggle() },
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Debug Tools Section
        DrawerSection(title = "Debug Tools") {
            DrawerMenuItem(
                icon = Icons.Default.Build,
                title = "Test ADB Connection",
                subtitle = "Test desktop compiler bridge",
                onClick = onTestADBClick
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App Information Section
        DrawerSection(title = "About") {
            DrawerMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences",
                onClick = onSettingsClick
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App information & credits",
                onClick = onAboutClick
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        DrawerFooter()
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Kotlin Text Editor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Professional Code Editor",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawerSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        content()
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            trailing?.invoke()
        }
    }
}

@Composable
private fun DrawerFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Built with ❤️ using Jetpack Compose",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * About dialog content
 */
@Composable
fun AboutDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Kotlin Text Editor")
        },
        text = {
            Column {
                Text("Version 1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "A professional Android text editor built with:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Jetpack Compose UI")
                Text("• Sora Editor for syntax highlighting")
                Text("• Material Design 3")
                Text("• MVVM Architecture")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Supports 14+ programming languages with professional templates and advanced editing features.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Settings dialog content
 */
@Composable
fun SettingsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isAutoSaveEnabled: Boolean,
    onAutoSaveToggle: () -> Unit
) {
    if (!isVisible) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Settings")
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto-save",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Automatically save files after editing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = isAutoSaveEnabled,
                        onCheckedChange = { onAutoSaveToggle() }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "More settings coming soon...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
