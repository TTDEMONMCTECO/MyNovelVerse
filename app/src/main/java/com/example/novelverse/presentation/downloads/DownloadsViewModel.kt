package com.example.novelverse.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.domain.model.DownloadItem
import com.example.novelverse.domain.model.DownloadStatus
import com.example.novelverse.domain.usecase.DownloadUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DownloadsUiState(
    val allDownloads: List<DownloadItem> = emptyList(),
    val activeDownloads: List<DownloadItem> = emptyList(),
    val completedDownloads: List<DownloadItem> = emptyList(),
    val totalDownloadedChapters: Int = 0,
    val isLoading: Boolean = true
)

class DownloadsViewModel(
    private val downloadUseCases: DownloadUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadUseCases.getAll().collect { list ->
                val active = list.filter { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING }
                val completed = list.filter { it.status == DownloadStatus.COMPLETED }
                _uiState.update {
                    it.copy(
                        allDownloads = list,
                        activeDownloads = active,
                        completedDownloads = completed,
                        totalDownloadedChapters = completed.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun cancelDownload(id: Long) {
        viewModelScope.launch {
            downloadUseCases.cancel(id)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            downloadUseCases.clearCompleted()
        }
    }

    class Factory(private val downloadUseCases: DownloadUseCases) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DownloadsViewModel(downloadUseCases) as T
        }
    }
}
