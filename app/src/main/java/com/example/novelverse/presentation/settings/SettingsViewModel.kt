package com.example.novelverse.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.novelverse.core.datastore.PreferencesManager
import com.example.novelverse.core.datastore.UserSettings
import com.example.novelverse.core.scraper.JsoupNovelScraper
import com.example.novelverse.core.scraper.ScraperConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val isTestingScraper: Boolean = false,
    val testScraperResult: String? = null
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val scraper: JsoupNovelScraper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.userSettingsFlow.collect { settings ->
                _uiState.update { it.copy(userSettings = settings) }
            }
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(mode)
        }
    }

    fun updateAccentColor(accent: String) {
        viewModelScope.launch {
            preferencesManager.updateAccentColor(accent)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotifications(enabled)
        }
    }

    fun updateScraperConfig(baseUrl: String, titleSelector: String, contentSelector: String, chapterSelector: String) {
        viewModelScope.launch {
            preferencesManager.updateScraperConfig(baseUrl, titleSelector, contentSelector, chapterSelector)
        }
    }

    fun testScraper(testUrl: String) {
        if (testUrl.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingScraper = true, testScraperResult = null) }
            val config = ScraperConfig(
                baseUrl = _uiState.value.userSettings.scraperBaseUrl,
                chapterListSelector = _uiState.value.userSettings.scraperChapterListSelector,
                chapterContentSelector = _uiState.value.userSettings.scraperContentSelector
            )
            val result = scraper.fetchNovelChapterList(testUrl, config)
            if (result.isSuccess) {
                val list = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isTestingScraper = false,
                        testScraperResult = "Success! Found ${list.size} chapters. Sample: ${list.firstOrNull()?.title ?: "N/A"}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isTestingScraper = false,
                        testScraperResult = "Scraper test error: ${result.exceptionOrNull()?.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testScraperResult = null) }
    }

    class Factory(
        private val preferencesManager: PreferencesManager,
        private val scraper: JsoupNovelScraper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesManager, scraper) as T
        }
    }
}
