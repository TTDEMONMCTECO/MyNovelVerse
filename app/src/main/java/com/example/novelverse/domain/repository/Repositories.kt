package com.example.novelverse.domain.repository

import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NovelRepository {
    fun getAllNovels(): Flow<List<Novel>>
    fun getFeaturedNovels(): Flow<List<Novel>>
    fun getTrendingNovels(): Flow<List<Novel>>
    fun getPopularNovels(): Flow<List<Novel>>
    fun getLatestNovels(): Flow<List<Novel>>
    fun getNovelById(id: String): Flow<Novel?>
    suspend fun getNovelByIdOnce(id: String): Novel?
    fun searchNovels(query: String): Flow<List<Novel>>
    suspend fun refreshNovels(): NetworkResult<Unit>
    suspend fun syncNovelFromWeb(url: String): NetworkResult<Novel>
}

interface ReaderRepository {
    fun getChapters(novelId: String): Flow<List<Chapter>>
    fun getChapter(chapterId: String): Flow<Chapter?>
    suspend fun getChapterByIndex(novelId: String, index: Int): Chapter?
    suspend fun fetchChapterContent(chapterId: String): NetworkResult<String>
    suspend fun saveReadingProgress(novelId: String, chapterId: String, chapterTitle: String, chapterIndex: Int, position: Int)
    suspend fun markChapterRead(chapterId: String, isRead: Boolean)
    suspend fun addBookmark(novelId: String, chapterId: String, chapterTitle: String, paragraphIndex: Int, excerpt: String, note: String): Long
    suspend fun deleteBookmark(bookmarkId: Long)
    fun getBookmarks(novelId: String): Flow<List<Bookmark>>
    fun getAllBookmarks(): Flow<List<Bookmark>>
}

interface LibraryRepository {
    fun getLibraryNovels(category: LibraryCategory?): Flow<List<Novel>>
    fun getAllLibraryNovels(): Flow<List<Novel>>
    fun getReadingHistory(): Flow<List<Novel>>
    suspend fun updateLibraryCategory(novelId: String, inLibrary: Boolean, category: LibraryCategory?, isFavorite: Boolean)
    suspend fun toggleFavorite(novelId: String, isFavorite: Boolean)
}

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadItem>>
    fun getActiveDownloads(): Flow<List<DownloadItem>>
    fun getDownloadedChapterCount(novelId: String): Flow<Int>
    suspend fun enqueueChapterDownload(novelId: String, chapterId: String, novelTitle: String, chapterTitle: String, index: Int)
    suspend fun enqueueNovelDownloads(novelId: String, novelTitle: String, chapters: List<Chapter>)
    suspend fun pauseDownload(id: Long)
    suspend fun cancelDownload(id: Long)
    suspend fun clearCompletedDownloads()
    suspend fun deleteDownloadedChapters(novelId: String)
}

interface SearchRepository {
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveSearch(query: String)
    suspend fun removeSearch(query: String)
    suspend fun clearAllSearches()
}
