package com.example.novelverse.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.domain.model.Bookmark
import com.example.novelverse.domain.model.Chapter
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.usecase.BookmarkUseCases
import com.example.novelverse.domain.usecase.DownloadUseCases
import com.example.novelverse.domain.usecase.GetNovelDetailsUseCase
import com.example.novelverse.domain.usecase.LibraryUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NovelDetailsUiState(
    val novel: Novel? = null,
    val chapters: List<Chapter> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val isAscending: Boolean = true,
    val downloadedCount: Int = 0,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class NovelDetailsViewModel(
    private val novelId: String,
    private val getNovelDetailsUseCase: GetNovelDetailsUseCase,
    private val libraryUseCases: LibraryUseCases,
    private val bookmarkUseCases: BookmarkUseCases,
    private val downloadUseCases: DownloadUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovelDetailsUiState())
    val uiState: StateFlow<NovelDetailsUiState> = _uiState.asStateFlow()

    init {
        loadNovel()
    }

    private fun loadNovel() {
        viewModelScope.launch {
            combine(
                getNovelDetailsUseCase.getNovel(novelId),
                getNovelDetailsUseCase.getChapters(novelId),
                bookmarkUseCases.getBookmarks(novelId),
                downloadUseCases.getDownloadedCount(novelId)
            ) { novel, chapters, bookmarks, downloadedCount ->
                _uiState.update { state ->
                    val sortedChapters = if (state.isAscending) {
                        chapters.sortedBy { it.index }
                    } else {
                        chapters.sortedByDescending { it.index }
                    }
                    state.copy(
                        novel = novel,
                        chapters = sortedChapters,
                        bookmarks = bookmarks,
                        downloadedCount = downloadedCount,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun toggleSortOrder() {
        _uiState.update { state ->
            val newAscending = !state.isAscending
            val sorted = if (newAscending) {
                state.chapters.sortedBy { it.index }
            } else {
                state.chapters.sortedByDescending { it.index }
            }
            state.copy(isAscending = newAscending, chapters = sorted)
        }
    }

    fun updateLibraryCategory(category: LibraryCategory?) {
        viewModelScope.launch {
            val novel = _uiState.value.novel ?: return@launch
            val inLibrary = (category != null)
            libraryUseCases.setCategory(novel.id, inLibrary, category, novel.isFavorite)
            _uiState.update {
                it.copy(userMessage = if (inLibrary) "Added to ${category?.displayName}" else "Removed from Library")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val novel = _uiState.value.novel ?: return@launch
            val newFav = !novel.isFavorite
            libraryUseCases.toggleFavorite(novel.id, newFav)
        }
    }

    fun downloadChapter(chapter: Chapter) {
        viewModelScope.launch {
            val novel = _uiState.value.novel ?: return@launch
            downloadUseCases.downloadChapter(
                novelId = novel.id,
                chapterId = chapter.id,
                novelTitle = novel.title,
                chapterTitle = chapter.title,
                index = chapter.index
            )
            _uiState.update { it.copy(userMessage = "Downloaded ${chapter.title}") }
        }
    }

    fun downloadAllChapters() {
        viewModelScope.launch {
            val novel = _uiState.value.novel ?: return@launch
            val chapters = _uiState.value.chapters
            downloadUseCases.downloadAll(novel.id, novel.title, chapters)
            _uiState.update { it.copy(userMessage = "All ${chapters.size} chapters queued for offline reading.") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val novelId: String,
        private val getNovelDetailsUseCase: GetNovelDetailsUseCase,
        private val libraryUseCases: LibraryUseCases,
        private val bookmarkUseCases: BookmarkUseCases,
        private val downloadUseCases: DownloadUseCases
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NovelDetailsViewModel(
                novelId,
                getNovelDetailsUseCase,
                libraryUseCases,
                bookmarkUseCases,
                downloadUseCases
            ) as T
        }
    }
}
