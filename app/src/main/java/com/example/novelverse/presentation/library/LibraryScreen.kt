package com.example.novelverse.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.presentation.components.EmptyView
import com.example.novelverse.presentation.components.LoadingView
import com.example.novelverse.presentation.components.NovelCoverImage
import com.example.novelverse.presentation.components.RatingBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNovelClick: (String) -> Unit,
    onResumeReading: (String, String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
            // Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                edgePadding = 16.dp
            ) {
                LibraryTab.values().forEach { tab ->
                    Tab(
                        selected = (state.selectedTab == tab),
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.title) },
                        modifier = Modifier.testTag("library_tab_${tab.name}")
                    )
                }
            }

            val list = if (state.selectedTab == LibraryTab.HISTORY) state.historyNovels else state.novels

            if (state.isLoading) {
                LoadingView(message = "Loading library...")
            } else if (list.isEmpty()) {
                EmptyView(
                    title = "Your ${state.selectedTab.title} is Empty",
                    message = if (state.selectedTab == LibraryTab.HISTORY)
                        "Novels you read will automatically appear here."
                    else
                        "Explore novels and add them to your library to track reading progress.",
                    icon = Icons.Default.BookmarkBorder,
                    actionLabel = "Explore Novels",
                    onActionClick = onExploreClick
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list, key = { it.id }) { novel ->
                        LibraryNovelCard(
                            novel = novel,
                            onClick = { onNovelClick(novel.id) },
                            onResumeClick = {
                                val chId = novel.lastReadChapterId ?: "${novel.id}_ch_1"
                                onResumeReading(novel.id, chId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryNovelCard(
    novel: Novel,
    onClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("library_card_${novel.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            ) {
                NovelCoverImage(
                    coverResId = novel.coverResId,
                    coverUrl = novel.coverUrl,
                    title = novel.title,
                    modifier = Modifier.fillMaxSize()
                )

                RatingBadge(
                    rating = novel.rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )

                // Quick read play icon
                FilledIconButton(
                    onClick = onResumeClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = novel.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = novel.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Reading Progress
                val progressText = if (novel.lastReadChapterIndex > 0) {
                    "Ch. ${novel.lastReadChapterIndex} / ${novel.totalChapters}"
                } else {
                    "${novel.totalChapters} Chapters"
                }

                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
