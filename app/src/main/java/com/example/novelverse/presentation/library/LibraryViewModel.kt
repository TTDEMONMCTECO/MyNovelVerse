package com.example.novelverse.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.usecase.LibraryUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LibraryTab(val title: String, val category: LibraryCategory?) {
    READING("Reading", LibraryCategory.READING),
    PLAN_TO_READ("Plan to Read", LibraryCategory.PLAN_TO_READ),
    COMPLETED("Completed", LibraryCategory.COMPLETED),
    FAVORITES("Favorites", LibraryCategory.FAVORITES),
    HISTORY("History", null)
}

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.READING,
    val novels: List<Novel> = emptyList(),
    val historyNovels: List<Novel> = emptyList(),
    val isLoading: Boolean = true
)

class LibraryViewModel(
    private val libraryUseCases: LibraryUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.map { it.selectedTab }
                .distinctUntilChanged()
                .collectLatest { tab ->
                    if (tab == LibraryTab.HISTORY) {
                        libraryUseCases.getReadingHistory().collect { history ->
                            _uiState.update { it.copy(historyNovels = history, isLoading = false) }
                        }
                    } else {
                        libraryUseCases.getLibrary(tab.category).collect { novels ->
                            _uiState.update { it.copy(novels = novels, isLoading = false) }
                        }
                    }
                }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab, isLoading = true) }
    }

    fun removeNovel(novelId: String) {
        viewModelScope.launch {
            libraryUseCases.setCategory(novelId, inLibrary = false, category = null, isFavorite = false)
        }
    }

    class Factory(private val libraryUseCases: LibraryUseCases) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(libraryUseCases) as T
        }
    }
}
