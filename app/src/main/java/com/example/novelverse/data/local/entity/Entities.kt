package com.example.novelverse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.novelverse.domain.model.Bookmark
import com.example.novelverse.domain.model.Chapter
import com.example.novelverse.domain.model.DownloadItem
import com.example.novelverse.domain.model.DownloadStatus

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["novelId", "chapterIndex"], unique = true)]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val novelId: String,
    val chapterIndex: Int,
    val title: String,
    val content: String = "",
    val url: String = "",
    val releaseDate: String = "",
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val isBookmarked: Boolean = false
) {
    fun toDomain(): Chapter = Chapter(
        id = id,
        novelId = novelId,
        index = chapterIndex,
        title = title,
        content = content,
        url = url,
        releaseDate = releaseDate,
        isRead = isRead,
        isDownloaded = isDownloaded,
        isBookmarked = isBookmarked
    )

    companion object {
        fun fromDomain(chapter: Chapter): ChapterEntity = ChapterEntity(
            id = chapter.id,
            novelId = chapter.novelId,
            chapterIndex = chapter.index,
            title = chapter.title,
            content = chapter.content,
            url = chapter.url,
            releaseDate = chapter.releaseDate,
            isRead = chapter.isRead,
            isDownloaded = chapter.isDownloaded,
            isBookmarked = chapter.isBookmarked
        )
    }
}

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val novelId: String,
    val chapterId: String,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val excerpt: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Bookmark = Bookmark(
        id = id,
        novelId = novelId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        paragraphIndex = paragraphIndex,
        excerpt = excerpt,
        note = note,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(bookmark: Bookmark): BookmarkEntity = BookmarkEntity(
            id = bookmark.id,
            novelId = bookmark.novelId,
            chapterId = bookmark.chapterId,
            chapterTitle = bookmark.chapterTitle,
            paragraphIndex = bookmark.paragraphIndex,
            excerpt = bookmark.excerpt,
            note = bookmark.note,
            createdAt = bookmark.createdAt
        )
    }
}

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val novelId: String,
    val chapterId: String,
    val novelTitle: String,
    val chapterTitle: String,
    val chapterIndex: Int,
    val status: String = "PENDING",
    val progress: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): DownloadItem = DownloadItem(
        id = id,
        novelId = novelId,
        chapterId = chapterId,
        novelTitle = novelTitle,
        chapterTitle = chapterTitle,
        chapterIndex = chapterIndex,
        status = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.PENDING),
        progress = progress,
        errorMessage = errorMessage,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(item: DownloadItem): DownloadEntity = DownloadEntity(
            id = item.id,
            novelId = item.novelId,
            chapterId = item.chapterId,
            novelTitle = item.novelTitle,
            chapterTitle = item.chapterTitle,
            chapterIndex = item.chapterIndex,
            status = item.status.name,
            progress = item.progress,
            errorMessage = item.errorMessage,
            timestamp = item.timestamp
        )
    }
}
