package com.example.novelverse.data.repository

import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.core.scraper.JsoupNovelScraper
import com.example.novelverse.core.scraper.ScraperConfig
import com.example.novelverse.data.local.dao.BookmarkDao
import com.example.novelverse.data.local.dao.ChapterDao
import com.example.novelverse.data.local.dao.NovelDao
import com.example.novelverse.data.local.entity.BookmarkEntity
import com.example.novelverse.domain.model.Bookmark
import com.example.novelverse.domain.model.Chapter
import com.example.novelverse.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReaderRepositoryImpl(
    private val chapterDao: ChapterDao,
    private val novelDao: NovelDao,
    private val bookmarkDao: BookmarkDao,
    private val scraper: JsoupNovelScraper
) : ReaderRepository {

    override fun getChapters(novelId: String): Flow<List<Chapter>> =
        chapterDao.getChaptersForNovel(novelId).map { list -> list.map { it.toDomain() } }

    override fun getChapter(chapterId: String): Flow<Chapter?> =
        chapterDao.getChapterById(chapterId).map { it?.toDomain() }

    override suspend fun getChapterByIndex(novelId: String, index: Int): Chapter? =
        chapterDao.getChapterByIndex(novelId, index)?.toDomain()

    override suspend fun fetchChapterContent(chapterId: String): NetworkResult<String> {
        val chapter = chapterDao.getChapterByIdOnce(chapterId)
            ?: return NetworkResult.Error("Chapter not found: $chapterId")

        // If content is already cached/downloaded in Room
        if (chapter.content.isNotBlank()) {
            return NetworkResult.Success(chapter.content)
        }

        // If chapter has an external URL, scrape it via Jsoup
        if (chapter.url.isNotBlank() && chapter.url.startsWith("http")) {
            val scrapedResult = scraper.fetchChapterContent(chapter.url, ScraperConfig())
            if (scrapedResult.isSuccess) {
                val scraped = scrapedResult.getOrThrow()
                chapterDao.updateDownloadedContent(chapterId, scraped.rawText)
                return NetworkResult.Success(scraped.rawText)
            }
        }

        // Otherwise generate formatted content
        val fallbackContent = "Content for ${chapter.title} is currently unavailable offline. Please check your connection or download this chapter."
        return NetworkResult.Success(fallbackContent)
    }

    override suspend fun saveReadingProgress(
        novelId: String,
        chapterId: String,
        chapterTitle: String,
        chapterIndex: Int,
        position: Int
    ) {
        val now = System.currentTimeMillis()
        novelDao.updateReadingProgress(novelId, chapterId, chapterTitle, chapterIndex, position, now)
        chapterDao.markChapterAsRead(chapterId, true)
    }

    override suspend fun markChapterRead(chapterId: String, isRead: Boolean) {
        chapterDao.markChapterAsRead(chapterId, isRead)
    }

    override suspend fun addBookmark(
        novelId: String,
        chapterId: String,
        chapterTitle: String,
        paragraphIndex: Int,
        excerpt: String,
        note: String
    ): Long {
        val entity = BookmarkEntity(
            novelId = novelId,
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            paragraphIndex = paragraphIndex,
            excerpt = excerpt,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        chapterDao.markChapterBookmarked(chapterId, true)
        return bookmarkDao.insertBookmark(entity)
    }

    override suspend fun deleteBookmark(bookmarkId: Long) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }

    override fun getBookmarks(novelId: String): Flow<List<Bookmark>> =
        bookmarkDao.getBookmarksForNovel(novelId).map { list -> list.map { it.toDomain() } }

    override fun getAllBookmarks(): Flow<List<Bookmark>> =
        bookmarkDao.getAllBookmarks().map { list -> list.map { it.toDomain() } }
}
