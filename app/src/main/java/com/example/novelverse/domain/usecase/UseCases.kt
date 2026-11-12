package com.example.novelverse.domain.usecase

import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.domain.model.*
import com.example.novelverse.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNovelsUseCase(private val repository: NovelRepository) {
    fun getAll(): Flow<List<Novel>> = repository.getAllNovels()
    fun getFeatured(): Flow<List<Novel>> = repository.getFeaturedNovels()
    fun getTrending(): Flow<List<Novel>> = repository.getTrendingNovels()
    fun getPopular(): Flow<List<Novel>> = repository.getPopularNovels()
    fun getLatest(): Flow<List<Novel>> = repository.getLatestNovels()
    fun search(query: String): Flow<List<Novel>> = repository.searchNovels(query)
    suspend fun refresh(): NetworkResult<Unit> = repository.refreshNovels()
    suspend fun scrapeUrl(url: String): NetworkResult<Novel> = repository.syncNovelFromWeb(url)
}

class GetNovelDetailsUseCase(
    private val novelRepository: NovelRepository,
    private val readerRepository: ReaderRepository
) {
    fun getNovel(id: String): Flow<Novel?> = novelRepository.getNovelById(id)
    fun getChapters(novelId: String): Flow<List<Chapter>> = readerRepository.getChapters(novelId)
    suspend fun getNovelOnce(id: String): Novel? = novelRepository.getNovelByIdOnce(id)
}

class GetChapterContentUseCase(private val repository: ReaderRepository) {
    fun getChapter(chapterId: String): Flow<Chapter?> = repository.getChapter(chapterId)
    suspend fun getChapterByIndex(novelId: String, index: Int): Chapter? = repository.getChapterByIndex(novelId, index)
    suspend fun fetchContent(chapterId: String): NetworkResult<String> = repository.fetchChapterContent(chapterId)
    suspend fun saveProgress(novelId: String, chapterId: String, title: String, index: Int, position: Int) {
        repository.saveReadingProgress(novelId, chapterId, title, index, position)
    }
    suspend fun markRead(chapterId: String, isRead: Boolean) = repository.markChapterRead(chapterId, isRead)
}

class LibraryUseCases(private val repository: LibraryRepository) {
    fun getLibrary(category: LibraryCategory?): Flow<List<Novel>> = repository.getLibraryNovels(category)
    fun getReadingHistory(): Flow<List<Novel>> = repository.getReadingHistory()
    suspend fun setCategory(novelId: String, inLibrary: Boolean, category: LibraryCategory?, isFavorite: Boolean) {
        repository.updateLibraryCategory(novelId, inLibrary, category, isFavorite)
    }
    suspend fun toggleFavorite(novelId: String, isFavorite: Boolean) {
        repository.toggleFavorite(novelId, isFavorite)
    }
}

class BookmarkUseCases(private val repository: ReaderRepository) {
    fun getBookmarks(novelId: String): Flow<List<Bookmark>> = repository.getBookmarks(novelId)
    fun getAllBookmarks(): Flow<List<Bookmark>> = repository.getAllBookmarks()
    suspend fun addBookmark(novelId: String, chapterId: String, chapterTitle: String, paragraphIndex: Int, excerpt: String, note: String): Long {
        return repository.addBookmark(novelId, chapterId, chapterTitle, paragraphIndex, excerpt, note)
    }
    suspend fun removeBookmark(id: Long) = repository.deleteBookmark(id)
}

class DownloadUseCases(private val repository: DownloadRepository) {
    fun getAll(): Flow<List<DownloadItem>> = repository.getAllDownloads()
    fun getActive(): Flow<List<DownloadItem>> = repository.getActiveDownloads()
    fun getDownloadedCount(novelId: String): Flow<Int> = repository.getDownloadedChapterCount(novelId)
    suspend fun downloadChapter(novelId: String, chapterId: String, novelTitle: String, chapterTitle: String, index: Int) {
        repository.enqueueChapterDownload(novelId, chapterId, novelTitle, chapterTitle, index)
    }
    suspend fun downloadAll(novelId: String, novelTitle: String, chapters: List<Chapter>) {
        repository.enqueueNovelDownloads(novelId, novelTitle, chapters)
    }
    suspend fun cancel(id: Long) = repository.cancelDownload(id)
    suspend fun clearCompleted() = repository.clearCompletedDownloads()
    suspend fun deleteDownloadedChapters(novelId: String) = repository.deleteDownloadedChapters(novelId)
}
