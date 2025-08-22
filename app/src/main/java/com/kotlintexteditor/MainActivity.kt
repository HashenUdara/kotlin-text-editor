package com.kotlintexteditor

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kotlintexteditor.data.FileManager
import com.kotlintexteditor.ui.editor.CodeEditorView
import com.kotlintexteditor.ui.editor.EditorLanguage
import com.kotlintexteditor.ui.editor.EditorState
import com.kotlintexteditor.ui.editor.TextEditorViewModel
import com.kotlintexteditor.ui.editor.TextEditorUiState
import com.kotlintexteditor.ui.editor.TextOperationsToolbar
import com.kotlintexteditor.ui.editor.EnhancedFindReplaceDialog
import com.kotlintexteditor.ui.dialogs.NewFileDialog
import com.kotlintexteditor.ui.dialogs.FileBrowserDialog
import com.kotlintexteditor.ui.dialogs.LanguageConfigurationDialog
import com.kotlintexteditor.ui.components.NavigationDrawer
import com.kotlintexteditor.ui.components.AboutDialog
import com.kotlintexteditor.ui.components.SettingsDialog
import com.kotlintexteditor.ui.theme.KotlinTextEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinTextEditorTheme {
                TextEditorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TextEditorApp() {
    val viewModel: TextEditorViewModel = viewModel()
    val editorState by viewModel.editorState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectionState by viewModel.selectionState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val canPaste by viewModel.canPaste.collectAsState()
    
    // Drawer state for hamburger menu
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Local dialog states for drawer menu items
    var isAboutDialogVisible by remember { mutableStateOf(false) }
    var isSettingsDialogVisible by remember { mutableStateOf(false) }
    
    // Search state
    val searchQuery by viewModel.searchQuery.collectAsState()
    val replaceText by viewModel.replaceText.collectAsState()
    val isCaseSensitive by viewModel.isCaseSensitive.collectAsState()
    val isWholeWord by viewModel.isWholeWord.collectAsState()
    val isRegexEnabled by viewModel.isRegexEnabled.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchDialogVisible by viewModel.isSearchDialogVisible.collectAsState()
    
    // New File Dialog state
    val isNewFileDialogVisible by viewModel.isNewFileDialogVisible.collectAsState()
    
    // File Browser Dialog state
    val isFileBrowserDialogVisible by viewModel.isFileBrowserDialogVisible.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    
    // Language Configuration Dialog state
    val isLanguageConfigDialogVisible by viewModel.isLanguageConfigDialogVisible.collectAsState()
    
    // File operation launchers
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            viewModel.openFile(it)
        }
    }
    
    // Alternative launcher using GetContent (sometimes works better)
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            viewModel.openFile(it)
        }
    }
    
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { viewModel.saveFile(it) }
    }
    
    // Storage permission handling
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

        ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawer(
                    onFindReplaceClick = {
                        scope.launch { drawerState.close() }
                        viewModel.showFindReplaceDialog()
                    },
                    onLanguageConfigClick = {
                        scope.launch { drawerState.close() }
                        viewModel.showLanguageConfigDialog()
                    },
                    onAutoSaveToggle = {
                        viewModel.toggleAutoSave()
                    },
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        isAboutDialogVisible = true
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        isSettingsDialogVisible = true
                    },
                    isAutoSaveEnabled = uiState.autoSaveEnabled
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Kotlin Text Editor")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    },
                    actions = {
                        // New file button (primary action)
                        IconButton(onClick = { viewModel.showNewFileDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = "New File")
                        }

                        // Open file button (primary action)
                        IconButton(onClick = { viewModel.showFileBrowserDialog() }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Open File")
                        }

                        // Save file button (primary action)
                        IconButton(
                            onClick = {
                                if (uiState.currentFileUri != null) {
                                    viewModel.saveFile()
                                } else {
                                    // Save as new file - determine extension based on language
                                    val fileName = when (editorState.language) {
                                        EditorLanguage.KOTLIN -> "untitled.kt"
                                        EditorLanguage.JAVA -> "untitled.java"
                                        else -> "untitled.txt"
                                    }
                                    saveFileLauncher.launch(fileName)
                                }
                            },
                            enabled = editorState.isModified
                        ) {
                            Icon(
                                imageVector = if (uiState.currentFileUri != null) Icons.Default.Save else Icons.Default.SaveAs,
                                contentDescription = if (uiState.currentFileUri != null) "Save" else "Save As"
                            )
                        }
                    }
                )
            },
        bottomBar = {
            StatusBar(
                editorState = editorState,
                uiState = uiState,
                onClearError = viewModel::clearError,
                onClearStatus = viewModel::clearStatus
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Text operations toolbar
                TextOperationsToolbar(
                    canUndo = canUndo,
                    canRedo = canRedo,
                    canPaste = canPaste,
                    hasSelection = selectionState.hasSelection,
                    onCopy = viewModel::copyText,
                    onCut = viewModel::cutText,
                    onPaste = viewModel::pasteText,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onSelectAll = viewModel::selectAll,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Main editor area
                CodeEditorView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    initialText = editorState.text,
                    language = editorState.language,
                    onTextChanged = { newText ->
                        viewModel.updateText(newText)
                    },
                    onSelectionChanged = { start, end ->
                        viewModel.updateSelection(start, end)
                    }
                )
            }
        }

        // Enhanced Find & Replace Dialog
        EnhancedFindReplaceDialog(
                    isVisible = isSearchDialogVisible,
                    searchQuery = searchQuery,
                    replaceText = replaceText,
                    isCaseSensitive = isCaseSensitive,
                    isWholeWord = isWholeWord,
                    isRegexEnabled = isRegexEnabled,
                    searchResults = searchResults,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onReplaceTextChange = viewModel::updateReplaceText,
                    onCaseSensitiveChange = viewModel::updateCaseSensitive,
                    onWholeWordChange = viewModel::updateWholeWord,
                    onRegexEnabledChange = viewModel::updateRegexEnabled,
                    onFindNext = viewModel::findNext,
                    onFindPrevious = viewModel::findPrevious,
                    onReplace = viewModel::replaceCurrent,
                    onReplaceAll = viewModel::replaceAll,
                    onClose = viewModel::hideFindReplaceDialog
        )

        // New File Dialog
        NewFileDialog(
            isVisible = isNewFileDialogVisible,
            onDismiss = viewModel::hideNewFileDialog,
            onCreateFile = { language, fileName, template ->
                viewModel.createNewFile(language, fileName, template)
            },
            onCreateFileWithLocation = { language, fileName, template ->
                val suggestedFileName = viewModel.createNewFileWithSaveDialog(language, fileName, template)
                saveFileLauncher.launch(suggestedFileName)
            }
        )

        // File Browser Dialog
        FileBrowserDialog(
            isVisible = isFileBrowserDialogVisible,
            onDismiss = viewModel::hideFileBrowserDialog,
            onOpenFile = {
                viewModel.hideFileBrowserDialog()
                // Use OpenDocument approach
                openFileLauncher.launch(arrayOf("*/*"))
            },
            onOpenFileAlternative = {
                viewModel.hideFileBrowserDialog()
                // Use GetContent approach
                getContentLauncher.launch("*/*")
            },
            onOpenRecentFile = { recentFile ->
                viewModel.openRecentFile(recentFile)
            },
            recentFiles = recentFiles
        )

        // Language Configuration Dialog
        com.kotlintexteditor.ui.dialogs.LanguageConfigurationDialog(
            isVisible = isLanguageConfigDialogVisible,
            onDismiss = viewModel::hideLanguageConfigDialog
        )

        // About Dialog
        AboutDialog(
            isVisible = isAboutDialogVisible,
            onDismiss = { isAboutDialogVisible = false }
        )

        // Settings Dialog
        SettingsDialog(
            isVisible = isSettingsDialogVisible,
            onDismiss = { isSettingsDialogVisible = false },
            isAutoSaveEnabled = uiState.autoSaveEnabled,
            onAutoSaveToggle = viewModel::toggleAutoSave
        )
        }
    }
}

@Composable
fun StatusBar(
    editorState: EditorState,
    uiState: TextEditorUiState,
    onClearError: () -> Unit,
    onClearStatus: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File info and status
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = editorState.filePath ?: "Untitled.kt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (editorState.isModified) {
    Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (uiState.autoSaveEnabled) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Auto-save enabled",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                // Error or status message
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    LaunchedEffect(error) {
                        kotlinx.coroutines.delay(5000)
                        onClearError()
                    }
                }
                
                uiState.statusMessage?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    LaunchedEffect(status) {
                        kotlinx.coroutines.delay(3000)
                        onClearStatus()
                    }
                }
            }
            
            // Stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Lines: ${editorState.lineCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Words: ${editorState.wordCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Chars: ${editorState.characterCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Language indicator
                Text(
                    text = editorState.language.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TextEditorPreview() {
    KotlinTextEditorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Preview with sample state
            val sampleEditorState = EditorState(
                text = "// Sample Kotlin code\nfun main() {\n    println(\"Hello, World!\")\n}",
                language = EditorLanguage.KOTLIN,
                filePath = "example.kt",
                isModified = true
            )
            
            Column {
                TopAppBar(
                    title = { Text("Kotlin Text Editor") }
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    // Editor would go here
                }
                
                StatusBar(
                    editorState = sampleEditorState,
                    uiState = TextEditorUiState(),
                    onClearError = {},
                    onClearStatus = {}
                )
            }
        }
    }
}