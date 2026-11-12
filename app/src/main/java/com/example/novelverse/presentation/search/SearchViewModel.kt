package com.example.novelverse.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.repository.SearchRepository
import com.example.novelverse.domain.usecase.GetNovelsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searchResults: List<Novel> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val getNovelsUseCase: GetNovelsUseCase,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        // Load recent searches
        viewModelScope.launch {
            searchRepository.getRecentSearches().collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }

        // Search debounced query flow
        viewModelScope.launch {
            queryFlow
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                    } else {
                        _uiState.update { it.copy(isSearching = true) }
                        getNovelsUseCase.search(query).collect { results ->
                            _uiState.update { it.copy(searchResults = results, isSearching = false) }
                        }
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        queryFlow.value = newQuery
    }

    fun onSearchSubmitted(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchRepository.saveSearch(query)
            }
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            searchRepository.removeSearch(query)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            searchRepository.clearAllSearches()
        }
    }

    class Factory(
        private val getNovelsUseCase: GetNovelsUseCase,
        private val searchRepository: SearchRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(getNovelsUseCase, searchRepository) as T
        }
    }
}
