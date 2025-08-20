package com.kotlintexteditor.ui.editor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

/**
 * Manages text search and replace operations
 */
class SearchManager {
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _replaceText = MutableStateFlow("")
    val replaceText: StateFlow<String> = _replaceText.asStateFlow()
    
    private val _isCaseSensitive = MutableStateFlow(false)
    val isCaseSensitive: StateFlow<Boolean> = _isCaseSensitive.asStateFlow()
    
    private val _isWholeWord = MutableStateFlow(false)
    val isWholeWord: StateFlow<Boolean> = _isWholeWord.asStateFlow()
    
    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()
    
    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()
    
    private var currentText: String = ""
    
    /**
     * Show the find/replace dialog
     */
    fun showDialog() {
        _isDialogVisible.value = true
    }
    
    /**
     * Hide the find/replace dialog
     */
    fun hideDialog() {
        _isDialogVisible.value = false
    }
    
    /**
     * Update search query and perform search
     */
    fun updateSearchQuery(query: String, text: String) {
        _searchQuery.value = query
        currentText = text
        if (query.isNotEmpty()) {
            performSearch()
        } else {
            _searchResults.value = SearchResults()
        }
    }
    
    /**
     * Update replace text
     */
    fun updateReplaceText(replace: String) {
        _replaceText.value = replace
    }
    
    /**
     * Update case sensitivity setting
     */
    fun updateCaseSensitive(caseSensitive: Boolean) {
        _isCaseSensitive.value = caseSensitive
        if (_searchQuery.value.isNotEmpty()) {
            performSearch()
        }
    }
    
    /**
     * Update whole word setting
     */
    fun updateWholeWord(wholeWord: Boolean) {
        _isWholeWord.value = wholeWord
        if (_searchQuery.value.isNotEmpty()) {
            performSearch()
        }
    }
    
    /**
     * Update current text and refresh search if needed
     */
    fun updateText(text: String) {
        currentText = text
        if (_searchQuery.value.isNotEmpty()) {
            performSearch()
        }
    }
    
    /**
     * Find next match
     */
    fun findNext(): SearchResult {
        val results = _searchResults.value
        if (!results.hasMatches) {
            return SearchResult.NotFound("No matches found")
        }
        
        val nextIndex = if (results.currentIndex + 1 < results.totalMatches) {
            results.currentIndex + 1
        } else {
            0 // Wrap to beginning
        }
        
        _searchResults.value = results.copy(currentIndex = nextIndex)
        val match = results.matches[nextIndex]
        
        return SearchResult.Found(
            match = match,
            position = nextIndex + 1,
            total = results.totalMatches
        )
    }
    
    /**
     * Find previous match
     */
    fun findPrevious(): SearchResult {
        val results = _searchResults.value
        if (!results.hasMatches) {
            return SearchResult.NotFound("No matches found")
        }
        
        val prevIndex = if (results.currentIndex - 1 >= 0) {
            results.currentIndex - 1
        } else {
            results.totalMatches - 1 // Wrap to end
        }
        
        _searchResults.value = results.copy(currentIndex = prevIndex)
        val match = results.matches[prevIndex]
        
        return SearchResult.Found(
            match = match,
            position = prevIndex + 1,
            total = results.totalMatches
        )
    }
    
    /**
     * Replace current match
     */
    fun replaceCurrent(): ReplaceResult {
        val results = _searchResults.value
        val replaceText = _replaceText.value
        
        if (!results.hasCurrentMatch) {
            return ReplaceResult.Error("No current match to replace")
        }
        
        val match = results.matches[results.currentIndex]
        val newText = currentText.replaceRange(
            match.startIndex,
            match.endIndex,
            replaceText
        )
        
        return ReplaceResult.Success(
            newText = newText,
            newCursorPosition = match.startIndex + replaceText.length,
            replacedText = match.text,
            position = results.currentIndex + 1,
            total = results.totalMatches
        )
    }
    
    /**
     * Replace all matches
     */
    fun replaceAll(): ReplaceResult {
        val results = _searchResults.value
        val replaceText = _replaceText.value
        
        if (!results.hasMatches) {
            return ReplaceResult.Error("No matches to replace")
        }
        
        var newText = currentText
        var offset = 0
        var replacedCount = 0
        
        // Replace from end to beginning to maintain indices
        results.matches.sortedByDescending { it.startIndex }.forEach { match ->
            val adjustedStart = match.startIndex + offset
            val adjustedEnd = match.endIndex + offset
            
            newText = newText.replaceRange(adjustedStart, adjustedEnd, replaceText)
            offset += replaceText.length - (match.endIndex - match.startIndex)
            replacedCount++
        }
        
        return ReplaceResult.Success(
            newText = newText,
            newCursorPosition = 0,
            replacedText = "($replacedCount matches)",
            position = replacedCount,
            total = replacedCount
        )
    }
    
    /**
     * Perform search with current settings
     */
    private fun performSearch() {
        val query = _searchQuery.value
        if (query.isEmpty() || currentText.isEmpty()) {
            _searchResults.value = SearchResults()
            return
        }
        
        try {
            val pattern = createSearchPattern(query)
            val matches = findMatches(pattern, currentText)
            
            _searchResults.value = SearchResults(
                totalMatches = matches.size,
                currentIndex = if (matches.isNotEmpty()) 0 else -1,
                matches = matches
            )
        } catch (e: Exception) {
            _searchResults.value = SearchResults()
        }
    }
    
    /**
     * Create regex pattern based on search settings
     */
    private fun createSearchPattern(query: String): Pattern {
        var patternString = if (_isWholeWord.value) {
            "\\b${Pattern.quote(query)}\\b"
        } else {
            Pattern.quote(query)
        }
        
        val flags = if (_isCaseSensitive.value) {
            0
        } else {
            Pattern.CASE_INSENSITIVE
        }
        
        return Pattern.compile(patternString, flags)
    }
    
    /**
     * Find all matches in text
     */
    private fun findMatches(pattern: Pattern, text: String): List<SearchMatch> {
        val matches = mutableListOf<SearchMatch>()
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            val matchText = text.substring(startIndex, endIndex)
            val lineNumber = getLineNumber(text, startIndex)
            
            matches.add(
                SearchMatch(
                    startIndex = startIndex,
                    endIndex = endIndex,
                    text = matchText,
                    lineNumber = lineNumber
                )
            )
        }
        
        return matches
    }
    
    /**
     * Get line number for a given text position
     */
    private fun getLineNumber(text: String, position: Int): Int {
        return text.substring(0, minOf(position, text.length)).count { it == '\n' } + 1
    }
    
    /**
     * Clear search state
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _replaceText.value = ""
        _searchResults.value = SearchResults()
    }
    
    /**
     * Get current match for highlighting
     */
    fun getCurrentMatch(): SearchMatch? {
        val results = _searchResults.value
        return if (results.hasCurrentMatch) {
            results.matches[results.currentIndex]
        } else {
            null
        }
    }
}

/**
 * Result of a search operation
 */
sealed class SearchResult {
    data class Found(
        val match: SearchMatch,
        val position: Int,
        val total: Int
    ) : SearchResult()
    
    data class NotFound(val message: String) : SearchResult()
}

/**
 * Result of a replace operation
 */
sealed class ReplaceResult {
    data class Success(
        val newText: String,
        val newCursorPosition: Int,
        val replacedText: String,
        val position: Int,
        val total: Int
    ) : ReplaceResult()
    
    data class Error(val message: String) : ReplaceResult()
}
