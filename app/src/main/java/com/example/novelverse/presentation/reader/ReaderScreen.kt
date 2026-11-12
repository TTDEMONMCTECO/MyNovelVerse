package com.example.novelverse.presentation.reader

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novelverse.presentation.components.LoadingView
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.bookmarkNotice) {
        state.bookmarkNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBookmarkNotice()
        }
    }

    // Track scroll position to update reading progress
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                viewModel.updateReadingPosition(index)
            }
    }

    // Determine current reader colors from theme preset
    val currentThemePreset = remember(state.readerPreferences.readerTheme) {
        ReaderThemePresets.firstOrNull { it.name.equals(state.readerPreferences.readerTheme, ignoreCase = true) }
            ?: ReaderThemePresets[1] // Sepia default
    }

    val currentFontFamily = remember(state.readerPreferences.fontFamily) {
        getFontFamilyByName(state.readerPreferences.fontFamily)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentThemePreset.backgroundColor)
            .testTag("reader_screen_container")
    ) {
        if (state.isLoading) {
            LoadingView(
                message = "Opening chapter...",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Main Content: Scrollable Story Paragraphs
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.toggleControls()
                    },
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 80.dp,
                    bottom = 120.dp
                )
            ) {
                // Chapter Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.novel?.title ?: "NovelVerse",
                            style = MaterialTheme.typography.labelMedium,
                            color = currentThemePreset.textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.currentChapter?.title ?: "",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = currentThemePreset.textColor,
                            fontFamily = currentFontFamily
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            color = currentThemePreset.textColor.copy(alpha = 0.2f),
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }

                // Paragraphs
                itemsIndexed(state.paragraphs) { index, paragraph ->
                    Text(
                        text = paragraph,
                        color = currentThemePreset.textColor,
                        fontSize = state.readerPreferences.fontSizeSp.sp,
                        fontFamily = currentFontFamily,
                        lineHeight = (state.readerPreferences.fontSizeSp * state.readerPreferences.lineSpacingMultiplier).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = state.readerPreferences.paragraphSpacingDp.dp)
                    )
                }

                // Chapter Navigation Footer
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "End of ${state.currentChapter?.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentThemePreset.textColor.copy(alpha = 0.6f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.loadPreviousChapter() },
                                enabled = (state.currentChapter?.index ?: 1) > 1
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }

                            Button(
                                onClick = { viewModel.loadNextChapter() },
                                enabled = (state.currentChapter?.index ?: 0) < state.allChapters.size
                            ) {
                                Text("Next Chapter")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Software Brightness Dimmer Overlay
        if (state.readerPreferences.brightnessPercent < 100) {
            val alpha = (100 - state.readerPreferences.brightnessPercent) / 100f * 0.7f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
            )
        }

        // Animated Top Reader Bar
        AnimatedVisibility(
            visible = state.isControlsVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = state.currentChapter?.title ?: "Reader",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = state.novel?.title ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("reader_back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Bookmark action
                        IconButton(
                            onClick = { viewModel.showBookmarkDialog(true) },
                            modifier = Modifier.testTag("reader_bookmark_button")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Add Bookmark")
                        }
                        // Table of Contents
                        IconButton(
                            onClick = { viewModel.showChapterListSheet(true) },
                            modifier = Modifier.testTag("reader_toc_button")
                        ) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = "Table of Contents")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }

        // Animated Bottom Reader Bar
        AnimatedVisibility(
            visible = state.isControlsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Reading Progress Text and Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ch. ${state.currentChapter?.index ?: 1} of ${state.allChapters.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(state.progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = state.progressPercent,
                        onValueChange = { percent ->
                            val targetIndex = (percent * state.totalParagraphs).toInt()
                            viewModel.updateReadingPosition(targetIndex)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.loadPreviousChapter() },
                            enabled = (state.currentChapter?.index ?: 1) > 1
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = null)
                            Text("Prev")
                        }

                        // Settings trigger
                        FilledTonalButton(
                            onClick = { viewModel.showSettingsSheet(true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("reader_settings_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Format")
                        }

                        TextButton(
                            onClick = { viewModel.loadNextChapter() },
                            enabled = (state.currentChapter?.index ?: 0) < state.allChapters.size
                        ) {
                            Text("Next")
                            Icon(Icons.Default.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }

    // Reader Settings Modal Sheet
    if (state.showSettingsSheet) {
        ReaderSettingsSheet(
            preferences = state.readerPreferences,
            onPreferencesChange = { viewModel.updateReaderPreferences(it) },
            onDismiss = { viewModel.showSettingsSheet(false) }
        )
    }

    // Chapters Table of Contents Sheet
    if (state.showChapterListSheet) {
        ChapterListSheet(
            chapters = state.allChapters,
            currentChapterId = state.currentChapter?.id,
            onChapterSelect = { chapterId ->
                viewModel.loadChapter(chapterId)
            },
            onDismiss = { viewModel.showChapterListSheet(false) }
        )
    }

    // Add Bookmark Dialog
    if (state.showBookmarkDialog) {
        val excerpt = state.paragraphs.getOrNull(state.currentParagraphIndex)?.take(100)
            ?: state.currentChapter?.title ?: "Chapter Bookmark"
        AddBookmarkDialog(
            excerpt = excerpt,
            onConfirm = { note -> viewModel.addBookmark(note) },
            onDismiss = { viewModel.showBookmarkDialog(false) }
        )
    }
}
