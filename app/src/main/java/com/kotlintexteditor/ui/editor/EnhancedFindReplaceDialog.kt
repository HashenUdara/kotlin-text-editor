package com.kotlintexteditor.ui.editor

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Enhanced Find and Replace dialog with advanced features and improved UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFindReplaceDialog(
    isVisible: Boolean,
    searchQuery: String,
    replaceText: String,
    isCaseSensitive: Boolean,
    isWholeWord: Boolean,
    isRegexEnabled: Boolean = false,
    searchResults: SearchResults,
    onSearchQueryChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onCaseSensitiveChange: (Boolean) -> Unit,
    onWholeWordChange: (Boolean) -> Unit,
    onRegexEnabledChange: (Boolean) -> Unit = {},
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Section
                DialogHeaderSection(onClose = onClose)
                
                // Search Section
                SearchInputSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onFindNext = onFindNext,
                    onFindPrevious = onFindPrevious,
                    searchResults = searchResults
                )
                
                // Replace Section
                ReplaceInputSection(
                    replaceText = replaceText,
                    onReplaceTextChange = onReplaceTextChange,
                    onReplace = onReplace,
                    hasMatches = searchResults.totalMatches > 0
                )
                
                HorizontalDivider()
                
                // Advanced Options Section
                AdvancedOptionsSection(
                    isCaseSensitive = isCaseSensitive,
                    isWholeWord = isWholeWord,
                    isRegexEnabled = isRegexEnabled,
                    onCaseSensitiveChange = onCaseSensitiveChange,
                    onWholeWordChange = onWholeWordChange,
                    onRegexEnabledChange = onRegexEnabledChange
                )
                
                // Search Results Info
                SearchResultsInfoSection(searchResults = searchResults)
                
                HorizontalDivider()
                
                // Action Buttons Section
                ActionButtonsSection(
                    searchResults = searchResults,
                    onFindNext = onFindNext,
                    onFindPrevious = onFindPrevious,
                    onReplaceAll = onReplaceAll,
                    hasQuery = searchQuery.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun DialogHeaderSection(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Find & Replace",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Advanced text search and replacement",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchInputSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit = {},
    searchResults: SearchResults
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Find text") },
                placeholder = { Text("Enter text to search...") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onFindNext()
                        keyboardController?.hide()
                    }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
            
            // Quick action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onFindNext,
                    enabled = searchQuery.isNotEmpty(),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Find Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                OutlinedIconButton(
                    onClick = onFindPrevious,
                    enabled = searchQuery.isNotEmpty(),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Find Previous",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplaceInputSection(
    replaceText: String,
    onReplaceTextChange: (String) -> Unit,
    onReplace: () -> Unit,
    hasMatches: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Replace",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = replaceText,
                onValueChange = onReplaceTextChange,
                label = { Text("Replace with") },
                placeholder = { Text("Enter replacement text...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                trailingIcon = {
                    if (replaceText.isNotEmpty()) {
                        IconButton(onClick = { onReplaceTextChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
            
            FilledTonalIconButton(
                onClick = onReplace,
                enabled = hasMatches,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Replace Current",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AdvancedOptionsSection(
    isCaseSensitive: Boolean,
    isWholeWord: Boolean,
    isRegexEnabled: Boolean,
    onCaseSensitiveChange: (Boolean) -> Unit,
    onWholeWordChange: (Boolean) -> Unit,
    onRegexEnabledChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Search Options",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        
        // Options Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OptionCard(
                    icon = Icons.Outlined.TextFields,
                    title = "Case Sensitive",
                    subtitle = "Match letter case",
                    isSelected = isCaseSensitive,
                    onClick = { onCaseSensitiveChange(!isCaseSensitive) },
                    modifier = Modifier.weight(1f)
                )
                
                OptionCard(
                    icon = Icons.Outlined.SelectAll,
                    title = "Whole Word",
                    subtitle = "Match complete words",
                    isSelected = isWholeWord,
                    onClick = { onWholeWordChange(!isWholeWord) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OptionCard(
                icon = Icons.Outlined.Code,
                title = "Regular Expression",
                subtitle = "Use regex patterns for advanced search",
                isSelected = isRegexEnabled,
                onClick = { onRegexEnabledChange(!isRegexEnabled) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SearchResultsInfoSection(
    searchResults: SearchResults
) {
    AnimatedVisibility(
        visible = searchResults.totalMatches > 0 || searchResults.totalMatches == 0,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    searchResults.totalMatches == 0 -> MaterialTheme.colorScheme.errorContainer
                    searchResults.totalMatches > 0 -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            searchResults.totalMatches == 0 -> Icons.Default.SearchOff
                            searchResults.totalMatches > 0 -> Icons.Default.Search
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            searchResults.totalMatches == 0 -> MaterialTheme.colorScheme.onErrorContainer
                            searchResults.totalMatches > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Column {
                        val infoText = when {
                            searchResults.totalMatches == 0 -> "No matches found"
                            searchResults.totalMatches == 1 -> "1 match found"
                            else -> "${searchResults.totalMatches} matches found"
                        }
                        
                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                searchResults.totalMatches == 0 -> MaterialTheme.colorScheme.onErrorContainer
                                searchResults.totalMatches > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (searchResults.hasCurrentMatch) {
                            Text(
                                text = "Current: ${searchResults.currentIndex + 1} of ${searchResults.totalMatches}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                if (searchResults.totalMatches > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${searchResults.totalMatches}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    searchResults: SearchResults,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplaceAll: () -> Unit,
    hasQuery: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onFindPrevious,
                enabled = searchResults.totalMatches > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Previous",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            OutlinedButton(
                onClick = onFindNext,
                enabled = searchResults.totalMatches > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        // Replace All button
        Button(
            onClick = onReplaceAll,
            enabled = searchResults.totalMatches > 0,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FindReplace,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Replace All (${searchResults.totalMatches} matches)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
