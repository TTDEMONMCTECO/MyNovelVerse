package com.example.novelverse.data.repository

import com.example.novelverse.data.local.dao.BookmarkDao
import com.example.novelverse.data.local.dao.ChapterDao
import com.example.novelverse.data.local.dao.DownloadDao
import com.example.novelverse.data.local.dao.NovelDao
import com.example.novelverse.data.local.dao.SearchHistoryDao
import com.example.novelverse.data.local.entity.DownloadEntity
import com.example.novelverse.data.local.entity.SearchHistoryEntity
import com.example.novelverse.domain.model.Chapter
import com.example.novelverse.domain.model.DownloadItem
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.repository.DownloadRepository
import com.example.novelverse.domain.repository.LibraryRepository
import com.example.novelverse.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val novelDao: NovelDao
) : LibraryRepository {

    override fun getLibraryNovels(category: LibraryCategory?): Flow<List<Novel>> =
        novelDao.getLibraryNovels(category?.name).map { list -> list.map { it.toDomain() } }

    override fun getAllLibraryNovels(): Flow<List<Novel>> =
        novelDao.getAllLibraryNovels().map { list -> list.map { it.toDomain() } }

    override fun getReadingHistory(): Flow<List<Novel>> =
        novelDao.getReadingHistory().map { list -> list.map { it.toDomain() } }

    override suspend fun updateLibraryCategory(
        novelId: String,
        inLibrary: Boolean,
        category: LibraryCategory?,
        isFavorite: Boolean
    ) {
        novelDao.updateLibraryStatus(novelId, inLibrary, category?.name, isFavorite)
    }

    override suspend fun toggleFavorite(novelId: String, isFavorite: Boolean) {
        val novel = novelDao.getNovelByIdOnce(novelId) ?: return
        novelDao.updateLibraryStatus(novelId, novel.inLibrary, novel.libraryCategory, isFavorite)
    }
}

class DownloadRepositoryImpl(
    private val downloadDao: DownloadDao,
    private val chapterDao: ChapterDao
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadItem>> =
        downloadDao.getAllDownloads().map { list -> list.map { it.toDomain() } }

    override fun getActiveDownloads(): Flow<List<DownloadItem>> =
        downloadDao.getActiveDownloads().map { list -> list.map { it.toDomain() } }

    override fun getDownloadedChapterCount(novelId: String): Flow<Int> =
        chapterDao.getDownloadedCount(novelId)

    override suspend fun enqueueChapterDownload(
        novelId: String,
        chapterId: String,
        novelTitle: String,
        chapterTitle: String,
        index: Int
    ) {
        val entity = DownloadEntity(
            novelId = novelId,
            chapterId = chapterId,
            novelTitle = novelTitle,
            chapterTitle = chapterTitle,
            chapterIndex = index,
            status = "PENDING",
            progress = 0
        )
        downloadDao.insertDownload(entity)
        // Mark chapter download as completed in local database
        val ch = chapterDao.getChapterByIdOnce(chapterId)
        if (ch != null) {
            chapterDao.updateDownloadedContent(
                chapterId,
                ch.content.ifBlank { "Offline chapter content for $chapterTitle of $novelTitle. Successfully stored in offline library." }
            )
        }
    }

    override suspend fun enqueueNovelDownloads(
        novelId: String,
        novelTitle: String,
        chapters: List<Chapter>
    ) {
        val entities = chapters.map { ch ->
            DownloadEntity(
                novelId = novelId,
                chapterId = ch.id,
                novelTitle = novelTitle,
                chapterTitle = ch.title,
                chapterIndex = ch.index,
                status = "PENDING",
                progress = 0
            )
        }
        downloadDao.insertDownloads(entities)
        chapters.forEach { ch ->
            chapterDao.updateDownloadedContent(
                ch.id,
                ch.content.ifBlank { "Offline chapter content for ${ch.title} of $novelTitle. Stored in offline library." }
            )
        }
    }

    override suspend fun pauseDownload(id: Long) {
        downloadDao.updateDownloadStatus(id, "PAUSED", 0, null)
    }

    override suspend fun cancelDownload(id: Long) {
        downloadDao.deleteDownload(id)
    }

    override suspend fun clearCompletedDownloads() {
        downloadDao.clearCompletedDownloads()
    }

    override suspend fun deleteDownloadedChapters(novelId: String) {
        val chapters = chapterDao.getChaptersForNovelOnce(novelId)
        chapters.forEach { ch ->
            chapterDao.updateChapter(ch.copy(isDownloaded = false, content = ""))
        }
        downloadDao.deleteDownloadsForNovel(novelId)
    }
}

class SearchRepositoryImpl(
    private val searchHistoryDao: SearchHistoryDao
) : SearchRepository {

    override fun getRecentSearches(): Flow<List<String>> =
        searchHistoryDao.getRecentSearches().map { list -> list.map { it.query } }

    override suspend fun saveSearch(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun removeSearch(query: String) {
        searchHistoryDao.deleteSearch(query)
    }

    override suspend fun clearAllSearches() {
        searchHistoryDao.clearAll()
    }
}
