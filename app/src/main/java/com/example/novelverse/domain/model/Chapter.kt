package com.example.novelverse.domain.model

data class Chapter(
    val id: String,
    val novelId: String,
    val index: Int,
    val title: String,
    val content: String = "",
    val url: String = "",
    val releaseDate: String = "",
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val isBookmarked: Boolean = false
)

data class Bookmark(
    val id: Long = 0,
    val novelId: String,
    val chapterId: String,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val excerpt: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED
}

data class DownloadItem(
    val id: Long = 0,
    val novelId: String,
    val chapterId: String,
    val novelTitle: String,
    val chapterTitle: String,
    val chapterIndex: Int,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
