package com.kotlintexteditor.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * File browser dialog for opening files
 */
@Composable
fun FileBrowserDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenFileAlternative: () -> Unit = {},
    onOpenRecentFile: (RecentFile) -> Unit = {},
    recentFiles: List<RecentFile> = emptyList()
) {
    if (!isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Dialog Header
                DialogHeader(
                    title = "Open File",
                    subtitle = "Browse files or select from recent files",
                    onDismiss = onDismiss
                )

                // Browse Files Section
                BrowseFilesSection(
                    onOpenFile = onOpenFile,
                    onOpenFileAlternative = onOpenFileAlternative
                )

                HorizontalDivider()

                // Recent Files Section
                RecentFilesSection(
                    recentFiles = recentFiles,
                    onOpenRecentFile = onOpenRecentFile
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BrowseFilesSection(
    onOpenFile: () -> Unit,
    onOpenFileAlternative: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Browse Files",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        // Browse button
        OutlinedButton(
            onClick = onOpenFile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Browse Device Storage",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }

        // Alternative browse button
        OutlinedButton(
            onClick = onOpenFileAlternative,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary
            ),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FindInPage,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Alternative File Picker",
                style = MaterialTheme.typography.labelMedium
            )
        }
        
        // Quick access buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessButton(
                icon = Icons.Default.Download,
                label = "Downloads",
                onClick = onOpenFile,
                modifier = Modifier.weight(1f)
            )
            QuickAccessButton(
                icon = Icons.Default.Folder,
                label = "Documents",
                onClick = onOpenFile,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentFilesSection(
    recentFiles: List<RecentFile>,
    onOpenRecentFile: (RecentFile) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Files",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (recentFiles.isNotEmpty()) {
                Text(
                    text = "${recentFiles.size} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (recentFiles.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No recent files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Recent files list
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentFiles.take(5)) { recentFile ->
                    RecentFileItem(
                        recentFile = recentFile,
                        onClick = { onOpenRecentFile(recentFile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentFileItem(
    recentFile: RecentFile,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getFileIcon(recentFile.extension),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getFileIconColor(recentFile.extension)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recentFile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = recentFile.lastModified,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getFileIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "kt", "kts" -> Icons.Default.Code
        "java" -> Icons.Default.Code
        "txt" -> Icons.Default.TextSnippet
        "md" -> Icons.Default.Article
        "json" -> Icons.Default.DataObject
        "xml" -> Icons.Default.DataObject
        else -> Icons.Default.InsertDriveFile
    }
}

@Composable
private fun getFileIconColor(extension: String): androidx.compose.ui.graphics.Color {
    return when (extension.lowercase()) {
        "kt", "kts" -> MaterialTheme.colorScheme.primary
        "java" -> MaterialTheme.colorScheme.tertiary
        "txt" -> MaterialTheme.colorScheme.secondary
        "md" -> MaterialTheme.colorScheme.primary
        "json", "xml" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Data class representing a recent file
 */
data class RecentFile(
    val name: String,
    val path: String,
    val extension: String,
    val lastModified: String,
    val size: String,
    val uri: String? = null
) {
    companion object {
        fun create(
            name: String,
            path: String,
            lastModifiedTimestamp: Long,
            sizeBytes: Long,
            uri: String? = null
        ): RecentFile {
            val extension = name.substringAfterLast('.', "")
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val lastModified = formatter.format(Date(lastModifiedTimestamp))
            val size = formatFileSize(sizeBytes)
            
            return RecentFile(
                name = name,
                path = path,
                extension = extension,
                lastModified = lastModified,
                size = size,
                uri = uri
            )
        }
        
        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }
    }
}
