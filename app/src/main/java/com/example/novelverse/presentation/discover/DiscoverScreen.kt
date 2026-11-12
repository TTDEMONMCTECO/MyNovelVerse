package com.example.novelverse.presentation.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.novelverse.presentation.components.EmptyView
import com.example.novelverse.presentation.components.LoadingView
import com.example.novelverse.presentation.components.NovelCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onNovelClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    var importUrlInput by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.importMessage) {
        state.importMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearImportMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discover Novels",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // Sort Menu
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("discover_sort_button")
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.selectSort(option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.sortBy == option) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Web Import Button
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("discover_import_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddLink, contentDescription = "Import from Web")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Genre Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availableGenres) { genre ->
                    FilterChip(
                        selected = (state.selectedGenre == genre),
                        onClick = { viewModel.selectGenre(genre) },
                        label = { Text(genre) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("chip_genre_$genre")
                    )
                }
            }

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf("All", "Ongoing", "Completed").forEach { status ->
                    FilterChip(
                        selected = (state.selectedStatus == status),
                        onClick = { viewModel.selectStatus(status) },
                        label = { Text(status) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Novels Grid
            if (state.isLoading) {
                LoadingView()
            } else if (state.filteredNovels.isEmpty()) {
                EmptyView(
                    title = "No Matching Novels",
                    message = "Try selecting a different genre or status filter.",
                    icon = Icons.Default.FilterList,
                    actionLabel = "Reset Filters",
                    onActionClick = {
                        viewModel.selectGenre("All")
                        viewModel.selectStatus("All")
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 135.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredNovels, key = { it.id }) { novel ->
                        NovelCard(
                            novel = novel,
                            onClick = { onNovelClick(novel.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Import from Web Modal Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Import Web Novel")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the URL of any authorized web novel website. NovelVerse uses Jsoup to parse chapters and add them to your offline library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importUrlInput,
                        onValueChange = { importUrlInput = it },
                        placeholder = { Text("https://novelfull.com/novel-name.html") },
                        label = { Text("Novel URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_url_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importFromWeb(importUrlInput)
                        showImportDialog = false
                        importUrlInput = ""
                    },
                    enabled = importUrlInput.isNotBlank() && !state.isImporting,
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text(if (state.isImporting) "Importing..." else "Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
