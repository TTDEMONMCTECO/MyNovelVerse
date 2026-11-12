package com.example.novelverse

import android.app.Application
import com.example.novelverse.di.AppContainer
import com.example.novelverse.di.DefaultAppContainer

class NovelVerseApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
