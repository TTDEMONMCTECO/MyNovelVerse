package com.example.novelverse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.model.NovelStatus

@Entity(tableName = "novels")
data class NovelEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverUrl: String,
    val coverResId: Int? = null,
    val genres: String, // comma-separated
    val status: String = "ONGOING",
    val rating: Float = 4.8f,
    val reviewCount: Int = 100,
    val totalChapters: Int = 0,
    val inLibrary: Boolean = false,
    val libraryCategory: String? = null,
    val lastReadChapterId: String? = null,
    val lastReadChapterTitle: String? = null,
    val lastReadChapterIndex: Int = 0,
    val lastReadPosition: Int = 0,
    val lastReadTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val sourceUrl: String? = null,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPopular: Boolean = false
) {
    fun toDomain(): Novel {
        return Novel(
            id = id,
            title = title,
            author = author,
            description = description,
            coverUrl = coverUrl,
            coverResId = coverResId,
            genres = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            status = NovelStatus.fromString(status),
            rating = rating,
            reviewCount = reviewCount,
            totalChapters = totalChapters,
            inLibrary = inLibrary,
            libraryCategory = libraryCategory?.let { LibraryCategory.fromString(it) },
            lastReadChapterId = lastReadChapterId,
            lastReadChapterTitle = lastReadChapterTitle,
            lastReadChapterIndex = lastReadChapterIndex,
            lastReadPosition = lastReadPosition,
            lastReadTimestamp = lastReadTimestamp,
            isFavorite = isFavorite,
            sourceUrl = sourceUrl,
            isFeatured = isFeatured,
            isTrending = isTrending,
            isPopular = isPopular
        )
    }

    companion object {
        fun fromDomain(novel: Novel): NovelEntity {
            return NovelEntity(
                id = novel.id,
                title = novel.title,
                author = novel.author,
                description = novel.description,
                coverUrl = novel.coverUrl,
                coverResId = novel.coverResId,
                genres = novel.genres.joinToString(","),
                status = novel.status.name,
                rating = novel.rating,
                reviewCount = novel.reviewCount,
                totalChapters = novel.totalChapters,
                inLibrary = novel.inLibrary,
                libraryCategory = novel.libraryCategory?.name,
                lastReadChapterId = novel.lastReadChapterId,
                lastReadChapterTitle = novel.lastReadChapterTitle,
                lastReadChapterIndex = novel.lastReadChapterIndex,
                lastReadPosition = novel.lastReadPosition,
                lastReadTimestamp = novel.lastReadTimestamp,
                isFavorite = novel.isFavorite,
                sourceUrl = novel.sourceUrl,
                isFeatured = novel.isFeatured,
                isTrending = novel.isTrending,
                isPopular = novel.isPopular
            )
        }
    }
}
