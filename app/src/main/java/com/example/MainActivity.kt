package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.novelverse.NovelVerseApplication
import com.example.novelverse.presentation.navigation.NovelVerseNavGraph
import com.example.ui.theme.NovelVerseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NovelVerseApplication
        val preferencesManager = app.container.preferencesManager

        setContent {
            val userSettings by preferencesManager.userSettingsFlow.collectAsState(
                initial = com.example.novelverse.core.datastore.UserSettings()
            )

            val isDark = when (userSettings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            NovelVerseTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NovelVerseNavGraph()
                }
            }
        }
    }
}
