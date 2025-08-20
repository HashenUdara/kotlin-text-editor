package com.kotlintexteditor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Find and Replace dialog with advanced search options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindReplaceDialog(
    isVisible: Boolean,
    searchQuery: String,
    replaceText: String,
    isCaseSensitive: Boolean,
    isWholeWord: Boolean,
    searchResults: SearchResults,
    onSearchQueryChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onCaseSensitiveChange: (Boolean) -> Unit,
    onWholeWordChange: (Boolean) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(onDismissRequest = onClose) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dialog Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Find & Replace",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    // Search Input
                    SearchInputSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onFindNext = onFindNext,
                        searchResults = searchResults
                    )
                    
                    // Replace Input
                    ReplaceInputSection(
                        replaceText = replaceText,
                        onReplaceTextChange = onReplaceTextChange,
                        onReplace = onReplace,
                        hasMatches = searchResults.totalMatches > 0
                    )
                    
                    // Search Options
                    SearchOptionsSection(
                        isCaseSensitive = isCaseSensitive,
                        isWholeWord = isWholeWord,
                        onCaseSensitiveChange = onCaseSensitiveChange,
                        onWholeWordChange = onWholeWordChange
                    )
                    
                    // Action Buttons
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
}

@Composable
private fun SearchInputSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    searchResults: SearchResults,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Find") },
                placeholder = { Text("Enter text to search...") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onFindNext()
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
            
            IconButton(
                onClick = onFindNext,
                enabled = searchQuery.isNotEmpty()
            ) {
                Icon(Icons.Default.Search, contentDescription = "Find Next")
            }
        }
        
        // Search Results Info
        if (searchQuery.isNotEmpty()) {
            SearchResultsInfo(searchResults = searchResults)
        }
    }
}

@Composable
private fun ReplaceInputSection(
    replaceText: String,
    onReplaceTextChange: (String) -> Unit,
    onReplace: () -> Unit,
    hasMatches: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = replaceText,
            onValueChange = onReplaceTextChange,
            label = { Text("Replace") },
            placeholder = { Text("Replace with...") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingIcon = {
                if (replaceText.isNotEmpty()) {
                    IconButton(onClick = { onReplaceTextChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )
        
        IconButton(
            onClick = onReplace,
            enabled = hasMatches
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Replace")
        }
    }
}

@Composable
private fun SearchOptionsSection(
    isCaseSensitive: Boolean,
    isWholeWord: Boolean,
    onCaseSensitiveChange: (Boolean) -> Unit,
    onWholeWordChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Search Options",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Case Sensitive
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isCaseSensitive,
                    onCheckedChange = onCaseSensitiveChange
                )
                Text(
                    text = "Case sensitive",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // Whole Word
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isWholeWord,
                    onCheckedChange = onWholeWordChange
                )
                Text(
                    text = "Whole word",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
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
    hasQuery: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Navigation buttons
        OutlinedButton(
            onClick = onFindPrevious,
            enabled = searchResults.totalMatches > 0,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Previous")
        }
        
        OutlinedButton(
            onClick = onFindNext,
            enabled = searchResults.totalMatches > 0,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Next")
        }
        
        // Replace All button
        Button(
            onClick = onReplaceAll,
            enabled = searchResults.totalMatches > 0,
            modifier = Modifier.weight(1f)
        ) {
            Text("Replace All")
        }
    }
}

@Composable
private fun SearchResultsInfo(
    searchResults: SearchResults,
    modifier: Modifier = Modifier
) {
    val infoText = when {
        searchResults.totalMatches == 0 -> "No matches found"
        searchResults.totalMatches == 1 -> "1 match found"
        else -> "${searchResults.currentIndex + 1} of ${searchResults.totalMatches} matches"
    }
    
    val textColor = when {
        searchResults.totalMatches == 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Text(
        text = infoText,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
        modifier = modifier.padding(top = 4.dp, start = 12.dp)
    )
}

/**
 * Data class representing search results
 */
data class SearchResults(
    val totalMatches: Int = 0,
    val currentIndex: Int = -1,
    val matches: List<SearchMatch> = emptyList()
) {
    val hasMatches: Boolean get() = totalMatches > 0
    val hasCurrentMatch: Boolean get() = currentIndex >= 0 && currentIndex < totalMatches
}

/**
 * Data class representing a single search match
 */
data class SearchMatch(
    val startIndex: Int,
    val endIndex: Int,
    val text: String,
    val lineNumber: Int
)
