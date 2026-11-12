package com.example.novelverse.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.core.datastore.PreferencesManager
import com.example.novelverse.core.datastore.ReaderPreferences
import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.domain.model.Chapter
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.usecase.BookmarkUseCases
import com.example.novelverse.domain.usecase.GetChapterContentUseCase
import com.example.novelverse.domain.usecase.GetNovelDetailsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReaderUiState(
    val novel: Novel? = null,
    val currentChapter: Chapter? = null,
    val allChapters: List<Chapter> = emptyList(),
    val paragraphs: List<String> = emptyList(),
    val readerPreferences: ReaderPreferences = ReaderPreferences(),
    val currentParagraphIndex: Int = 0,
    val totalParagraphs: Int = 0,
    val progressPercent: Float = 0f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isControlsVisible: Boolean = true,
    val showSettingsSheet: Boolean = false,
    val showChapterListSheet: Boolean = false,
    val showBookmarkDialog: Boolean = false,
    val bookmarkNotice: String? = null
)

class ReaderViewModel(
    private val novelId: String,
    private val initialChapterId: String,
    private val getChapterContentUseCase: GetChapterContentUseCase,
    private val getNovelDetailsUseCase: GetNovelDetailsUseCase,
    private val bookmarkUseCases: BookmarkUseCases,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        // Collect user preferences
        viewModelScope.launch {
            preferencesManager.readerPreferencesFlow.collect { prefs ->
                _uiState.update { it.copy(readerPreferences = prefs) }
            }
        }

        // Load novel and chapters
        viewModelScope.launch {
            getNovelDetailsUseCase.getNovel(novelId).collect { novel ->
                _uiState.update { it.copy(novel = novel) }
            }
        }

        viewModelScope.launch {
            getNovelDetailsUseCase.getChapters(novelId).collect { chapters ->
                _uiState.update { it.copy(allChapters = chapters.sortedBy { ch -> ch.index }) }
            }
        }

        loadChapter(initialChapterId)
    }

    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val chapter = getChapterContentUseCase.getChapter(chapterId).firstOrNull()
            if (chapter != null) {
                val contentResult = getChapterContentUseCase.fetchContent(chapterId)
                val fullText = when (contentResult) {
                    is NetworkResult.Success -> contentResult.data
                    is NetworkResult.Error -> chapter.content.ifBlank { "Could not load chapter text: ${contentResult.message}" }
                    NetworkResult.Loading -> chapter.content
                }

                val paragraphs = fullText.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                _uiState.update {
                    it.copy(
                        currentChapter = chapter,
                        paragraphs = paragraphs,
                        totalParagraphs = paragraphs.size,
                        currentParagraphIndex = 0,
                        progressPercent = 0f,
                        isLoading = false
                    )
                }

                // Save reading progress in background
                getChapterContentUseCase.saveProgress(
                    novelId = novelId,
                    chapterId = chapterId,
                    title = chapter.title,
                    index = chapter.index,
                    position = 0
                )
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Chapter not found") }
            }
        }
    }

    fun loadNextChapter() {
        val current = _uiState.value.currentChapter ?: return
        val next = _uiState.value.allChapters.firstOrNull { it.index == current.index + 1 }
        if (next != null) {
            loadChapter(next.id)
        }
    }

    fun loadPreviousChapter() {
        val current = _uiState.value.currentChapter ?: return
        val prev = _uiState.value.allChapters.firstOrNull { it.index == current.index - 1 }
        if (prev != null) {
            loadChapter(prev.id)
        }
    }

    fun toggleControls() {
        _uiState.update { it.copy(isControlsVisible = !it.isControlsVisible) }
    }

    fun setControlsVisible(visible: Boolean) {
        _uiState.update { it.copy(isControlsVisible = visible) }
    }

    fun updateReadingPosition(paragraphIndex: Int) {
        val total = _uiState.value.totalParagraphs
        val percent = if (total > 0) (paragraphIndex.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        _uiState.update {
            it.copy(currentParagraphIndex = paragraphIndex, progressPercent = percent)
        }
        val current = _uiState.value.currentChapter ?: return
        viewModelScope.launch {
            getChapterContentUseCase.saveProgress(
                novelId = novelId,
                chapterId = current.id,
                title = current.title,
                index = current.index,
                position = paragraphIndex
            )
        }
    }

    fun updateReaderPreferences(newPrefs: ReaderPreferences) {
        viewModelScope.launch {
            preferencesManager.updateReaderPreferences(newPrefs)
        }
    }

    fun showSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun showChapterListSheet(show: Boolean) {
        _uiState.update { it.copy(showChapterListSheet = show) }
    }

    fun showBookmarkDialog(show: Boolean) {
        _uiState.update { it.copy(showBookmarkDialog = show) }
    }

    fun addBookmark(note: String) {
        val current = _uiState.value.currentChapter ?: return
        val pIndex = _uiState.value.currentParagraphIndex
        val paragraphs = _uiState.value.paragraphs
        val excerpt = paragraphs.getOrNull(pIndex)?.take(120) ?: current.title

        viewModelScope.launch {
            bookmarkUseCases.addBookmark(
                novelId = novelId,
                chapterId = current.id,
                chapterTitle = current.title,
                paragraphIndex = pIndex,
                excerpt = excerpt,
                note = note
            )
            _uiState.update {
                it.copy(
                    showBookmarkDialog = false,
                    bookmarkNotice = "Bookmark saved at ${current.title}"
                )
            }
        }
    }

    fun clearBookmarkNotice() {
        _uiState.update { it.copy(bookmarkNotice = null) }
    }

    class Factory(
        private val novelId: String,
        private val chapterId: String,
        private val getChapterContentUseCase: GetChapterContentUseCase,
        private val getNovelDetailsUseCase: GetNovelDetailsUseCase,
        private val bookmarkUseCases: BookmarkUseCases,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(
                novelId,
                chapterId,
                getChapterContentUseCase,
                getNovelDetailsUseCase,
                bookmarkUseCases,
                preferencesManager
            ) as T
        }
    }
}
