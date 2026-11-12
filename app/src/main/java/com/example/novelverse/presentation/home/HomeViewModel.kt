package com.example.novelverse.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.usecase.GetNovelsUseCase
import com.example.novelverse.domain.usecase.LibraryUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val featuredNovels: List<Novel> = emptyList(),
    val trendingNovels: List<Novel> = emptyList(),
    val popularNovels: List<Novel> = emptyList(),
    val latestNovels: List<Novel> = emptyList(),
    val continueReadingNovel: Novel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val getNovelsUseCase: GetNovelsUseCase,
    private val libraryUseCases: LibraryUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getNovelsUseCase.getFeatured(),
                getNovelsUseCase.getTrending(),
                getNovelsUseCase.getPopular(),
                getNovelsUseCase.getLatest(),
                libraryUseCases.getReadingHistory()
            ) { featured, trending, popular, latest, history ->
                HomeUiState(
                    featuredNovels = featured.ifEmpty { latest.take(3) },
                    trendingNovels = trending,
                    popularNovels = popular,
                    latestNovels = latest,
                    continueReadingNovel = history.firstOrNull(),
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getNovelsUseCase.refresh()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    class Factory(
        private val getNovelsUseCase: GetNovelsUseCase,
        private val libraryUseCases: LibraryUseCases
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(getNovelsUseCase, libraryUseCases) as T
        }
    }
}
