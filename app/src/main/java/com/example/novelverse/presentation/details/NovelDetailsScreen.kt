package com.example.novelverse.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.presentation.components.ChapterListItem
import com.example.novelverse.presentation.components.LoadingView
import com.example.novelverse.presentation.components.NovelCoverImage
import com.example.novelverse.presentation.components.RatingBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailsScreen(
    viewModel: NovelDetailsViewModel,
    onChapterClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showCategoryBottomSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.novel?.title ?: "Novel Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("details_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("details_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (state.novel?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (state.novel?.isFavorite == true) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            state.novel?.let { novel ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Library Button
                        OutlinedButton(
                            onClick = { showCategoryBottomSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("details_library_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (novel.inLibrary) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = novel.libraryCategory?.displayName ?: "Add to Library",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Start / Continue Reading Button
                        Button(
                            onClick = {
                                val targetChapterId = novel.lastReadChapterId
                                    ?: state.chapters.firstOrNull()?.id
                                    ?: "${novel.id}_ch_1"
                                onChapterClick(novel.id, targetChapterId)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("details_read_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (novel.lastReadChapterIndex > 0) "Continue Ch. ${novel.lastReadChapterIndex}" else "Start Reading",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (state.isLoading || state.novel == null) {
            LoadingView(modifier = Modifier.padding(innerPadding))
        } else {
            val novel = state.novel!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Header section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(160.dp)
                            ) {
                                NovelCoverImage(
                                    coverResId = novel.coverResId,
                                    coverUrl = novel.coverUrl,
                                    title = novel.title,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 12.dp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = novel.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "By ${novel.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RatingBadge(rating = novel.rating)
                                    Text(
                                        text = "${novel.reviewCount} reviews",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Status badge
                                Surface(
                                    color = if (novel.status.name == "COMPLETED") Color(0xFF10B981) else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = novel.status.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (novel.status.name == "COMPLETED") Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Genre chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            novel.genres.take(4).forEach { genre ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Synopsis Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Synopsis",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = novel.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 20.sp
                                )
                                TextButton(
                                    onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(if (isDescriptionExpanded) "Show Less" else "Read More")
                                }
                            }
                        }
                    }
                }

                // Tabs: Chapters and Bookmarks
                item {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = (selectedTab == 0),
                            onClick = { selectedTab = 0 },
                            text = { Text("Chapters (${state.chapters.size})") }
                        )
                        Tab(
                            selected = (selectedTab == 1),
                            onClick = { selectedTab = 1 },
                            text = { Text("Bookmarks (${state.bookmarks.size})") }
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Chapters actions bar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.downloadedCount} / ${state.chapters.size} downloaded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.toggleSortOrder() },
                                    modifier = Modifier.testTag("sort_chapters_button")
                                ) {
                                    Icon(
                                        imageVector = if (state.isAscending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = "Sort order"
                                    )
                                }

                                TextButton(onClick = { viewModel.downloadAllChapters() }) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download All", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Chapters list
                    items(state.chapters, key = { it.id }) { chapter ->
                        ChapterListItem(
                            chapter = chapter,
                            onClick = { onChapterClick(novel.id, chapter.id) },
                            onDownloadClick = { viewModel.downloadChapter(chapter) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                } else {
                    // Bookmarks tab
                    if (state.bookmarks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No bookmarks yet. Tap the bookmark icon while reading any chapter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(state.bookmarks) { bookmark ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = bookmark.chapterTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${bookmark.excerpt}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (bookmark.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Note: ${bookmark.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Bottom Sheet
    if (showCategoryBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add to Library",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                LibraryCategory.values().forEach { category ->
                    val isSelected = state.novel?.libraryCategory == category
                    ListItem(
                        headlineContent = { Text(category.displayName) },
                        leadingContent = {
                            Icon(
                                imageVector = when (category) {
                                    LibraryCategory.READING -> Icons.Default.MenuBook
                                    LibraryCategory.PLAN_TO_READ -> Icons.Default.Schedule
                                    LibraryCategory.COMPLETED -> Icons.Default.CheckCircle
                                    LibraryCategory.FAVORITES -> Icons.Default.Favorite
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLibraryCategory(category)
                                showCategoryBottomSheet = false
                            }
                    )
                }

                if (state.novel?.inLibrary == true) {
                    ListItem(
                        headlineContent = { Text("Remove from Library", color = MaterialTheme.colorScheme.error) },
                        leadingContent = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLibraryCategory(null)
                                showCategoryBottomSheet = false
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
