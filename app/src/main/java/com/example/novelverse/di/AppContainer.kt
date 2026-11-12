package com.example.novelverse.di

import android.content.Context
import com.example.novelverse.core.datastore.PreferencesManager
import com.example.novelverse.core.network.AuthInterceptor
import com.example.novelverse.core.network.HeaderInterceptor
import com.example.novelverse.core.network.TokenManager
import com.example.novelverse.core.scraper.JsoupNovelScraper
import com.example.novelverse.data.local.NovelVerseDatabase
import com.example.novelverse.data.remote.NovelApiService
import com.example.novelverse.data.repository.*
import com.example.novelverse.domain.repository.*
import com.example.novelverse.domain.usecase.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val preferencesManager: PreferencesManager
    val tokenManager: TokenManager
    val novelRepository: NovelRepository
    val readerRepository: ReaderRepository
    val libraryRepository: LibraryRepository
    val downloadRepository: DownloadRepository
    val searchRepository: SearchRepository
    
    // Use Cases
    val getNovelsUseCase: GetNovelsUseCase
    val getNovelDetailsUseCase: GetNovelDetailsUseCase
    val getChapterContentUseCase: GetChapterContentUseCase
    val libraryUseCases: LibraryUseCases
    val bookmarkUseCases: BookmarkUseCases
    val downloadUseCases: DownloadUseCases
    val scraper: JsoupNovelScraper
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }

    override val tokenManager: TokenManager by lazy {
        TokenManager(preferencesManager)
    }

    private val headerInterceptor by lazy { HeaderInterceptor() }
    private val authInterceptor by lazy { AuthInterceptor(tokenManager) }
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.novelverse.example.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val apiService: NovelApiService by lazy {
        retrofit.create(NovelApiService::class.java)
    }

    override val scraper: JsoupNovelScraper by lazy {
        JsoupNovelScraper()
    }

    private val database: NovelVerseDatabase by lazy {
        NovelVerseDatabase.getInstance(context)
    }

    override val novelRepository: NovelRepository by lazy {
        NovelRepositoryImpl(
            novelDao = database.novelDao(),
            chapterDao = database.chapterDao(),
            apiService = apiService,
            scraper = scraper
        )
    }

    override val readerRepository: ReaderRepository by lazy {
        ReaderRepositoryImpl(
            chapterDao = database.chapterDao(),
            novelDao = database.novelDao(),
            bookmarkDao = database.bookmarkDao(),
            scraper = scraper
        )
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            novelDao = database.novelDao()
        )
    }

    override val downloadRepository: DownloadRepository by lazy {
        DownloadRepositoryImpl(
            downloadDao = database.downloadDao(),
            chapterDao = database.chapterDao()
        )
    }

    override val searchRepository: SearchRepository by lazy {
        SearchRepositoryImpl(
            searchHistoryDao = database.searchHistoryDao()
        )
    }

    override val getNovelsUseCase: GetNovelsUseCase by lazy {
        GetNovelsUseCase(novelRepository)
    }

    override val getNovelDetailsUseCase: GetNovelDetailsUseCase by lazy {
        GetNovelDetailsUseCase(novelRepository, readerRepository)
    }

    override val getChapterContentUseCase: GetChapterContentUseCase by lazy {
        GetChapterContentUseCase(readerRepository)
    }

    override val libraryUseCases: LibraryUseCases by lazy {
        LibraryUseCases(libraryRepository)
    }

    override val bookmarkUseCases: BookmarkUseCases by lazy {
        BookmarkUseCases(readerRepository)
    }

    override val downloadUseCases: DownloadUseCases by lazy {
        DownloadUseCases(downloadRepository)
    }
}
