package com.example.novelverse.data.local.dao

import androidx.room.*
import com.example.novelverse.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelDao {
    @Query("SELECT * FROM novels")
    fun getAllNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE isFeatured = 1")
    fun getFeaturedNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE isTrending = 1 ORDER BY rating DESC")
    fun getTrendingNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE isPopular = 1 ORDER BY reviewCount DESC")
    fun getPopularNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels ORDER BY lastReadTimestamp DESC")
    fun getLatestNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE inLibrary = 1 AND (:category IS NULL OR libraryCategory = :category) ORDER BY lastReadTimestamp DESC, title ASC")
    fun getLibraryNovels(category: String?): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE inLibrary = 1 ORDER BY lastReadTimestamp DESC")
    fun getAllLibraryNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE lastReadTimestamp > 0 ORDER BY lastReadTimestamp DESC LIMIT 10")
    fun getReadingHistory(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE id = :id")
    fun getNovelById(id: String): Flow<NovelEntity?>

    @Query("SELECT * FROM novels WHERE id = :id")
    suspend fun getNovelByIdOnce(id: String): NovelEntity?

    @Query("SELECT * FROM novels WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR genres LIKE '%' || :query || '%'")
    fun searchNovels(query: String): Flow<List<NovelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovels(novels: List<NovelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: NovelEntity)

    @Update
    suspend fun updateNovel(novel: NovelEntity)

    @Query("UPDATE novels SET inLibrary = :inLibrary, libraryCategory = :category, isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateLibraryStatus(id: String, inLibrary: Boolean, category: String?, isFavorite: Boolean)

    @Query("UPDATE novels SET lastReadChapterId = :chapterId, lastReadChapterTitle = :chapterTitle, lastReadChapterIndex = :chapterIndex, lastReadPosition = :position, lastReadTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReadingProgress(id: String, chapterId: String, chapterTitle: String, chapterIndex: Int, position: Int, timestamp: Long)

    @Query("DELETE FROM novels WHERE id = :id")
    suspend fun deleteNovel(id: String)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY chapterIndex ASC")
    fun getChaptersForNovel(novelId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY chapterIndex ASC")
    suspend fun getChaptersForNovelOnce(novelId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id")
    fun getChapterById(id: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterByIdOnce(id: String): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE novelId = :novelId AND chapterIndex = :index LIMIT 1")
    suspend fun getChapterByIndex(novelId: String, index: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET content = :content, isDownloaded = 1 WHERE id = :chapterId")
    suspend fun updateDownloadedContent(chapterId: String, content: String)

    @Query("UPDATE chapters SET isRead = :isRead WHERE id = :chapterId")
    suspend fun markChapterAsRead(chapterId: String, isRead: Boolean)

    @Query("UPDATE chapters SET isBookmarked = :isBookmarked WHERE id = :chapterId")
    suspend fun markChapterBookmarked(chapterId: String, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM chapters WHERE novelId = :novelId AND isDownloaded = 1")
    fun getDownloadedCount(novelId: String): Flow<Int>

    @Query("DELETE FROM chapters WHERE novelId = :novelId")
    suspend fun deleteChaptersForNovel(novelId: String)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE novelId = :novelId ORDER BY createdAt DESC")
    fun getBookmarksForNovel(novelId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM bookmarks WHERE chapterId = :chapterId")
    suspend fun deleteBookmarkForChapter(chapterId: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'DOWNLOADING') ORDER BY id ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'PENDING' ORDER BY id ASC LIMIT 1")
    suspend fun getNextPendingDownload(): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(items: List<DownloadEntity>)

    @Query("UPDATE downloads SET status = :status, progress = :progress, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateDownloadStatus(id: Long, status: String, progress: Int, errorMessage: String?)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompletedDownloads()

    @Query("DELETE FROM downloads WHERE novelId = :novelId")
    suspend fun deleteDownloadsForNovel(novelId: String)
}
