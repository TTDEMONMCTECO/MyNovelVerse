package com.example.novelverse.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.model.NovelStatus
import com.example.novelverse.domain.usecase.GetNovelsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val allNovels: List<Novel> = emptyList(),
    val filteredNovels: List<Novel> = emptyList(),
    val selectedGenre: String = "All",
    val selectedStatus: String = "All",
    val sortBy: SortOption = SortOption.RATING,
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val importMessage: String? = null
)

enum class SortOption(val displayName: String) {
    RATING("Top Rated"),
    POPULARITY("Most Popular"),
    CHAPTERS("Most Chapters"),
    TITLE("Alphabetical")
}

class DiscoverViewModel(
    private val getNovelsUseCase: GetNovelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    val availableGenres = listOf(
        "All", "Fantasy", "Sci-Fi", "Cultivation", "Xianxia",
        "Mystery", "LitRPG", "System", "Action", "Martial Arts", "Adventure"
    )

    init {
        loadNovels()
    }

    private fun loadNovels() {
        viewModelScope.launch {
            getNovelsUseCase.getAll().collect { novels ->
                _uiState.update { state ->
                    val filtered = applyFilters(novels, state.selectedGenre, state.selectedStatus, state.sortBy)
                    state.copy(
                        allNovels = novels,
                        filteredNovels = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectGenre(genre: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allNovels, genre, state.selectedStatus, state.sortBy)
            state.copy(selectedGenre = genre, filteredNovels = filtered)
        }
    }

    fun selectStatus(status: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allNovels, state.selectedGenre, status, state.sortBy)
            state.copy(selectedStatus = status, filteredNovels = filtered)
        }
    }

    fun selectSort(sort: SortOption) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allNovels, state.selectedGenre, state.selectedStatus, sort)
            state.copy(sortBy = sort, filteredNovels = filtered)
        }
    }

    fun importFromWeb(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importMessage = null) }
            val result = getNovelsUseCase.scrapeUrl(url)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importMessage = "Successfully imported: ${result.data.title}"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importMessage = "Import failed: ${result.message}"
                        )
                    }
                }
                NetworkResult.Loading -> {}
            }
        }
    }

    fun clearImportMessage() {
        _uiState.update { it.copy(importMessage = null) }
    }

    private fun applyFilters(
        novels: List<Novel>,
        genre: String,
        status: String,
        sort: SortOption
    ): List<Novel> {
        var list = novels

        if (genre != "All") {
            list = list.filter { novel -> novel.genres.any { it.equals(genre, ignoreCase = true) } }
        }

        if (status != "All") {
            list = list.filter { novel -> novel.status.displayName.equals(status, ignoreCase = true) }
        }

        return when (sort) {
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.POPULARITY -> list.sortedByDescending { it.reviewCount }
            SortOption.CHAPTERS -> list.sortedByDescending { it.totalChapters }
            SortOption.TITLE -> list.sortedBy { it.title }
        }
    }

    class Factory(private val getNovelsUseCase: GetNovelsUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoverViewModel(getNovelsUseCase) as T
        }
    }
}
