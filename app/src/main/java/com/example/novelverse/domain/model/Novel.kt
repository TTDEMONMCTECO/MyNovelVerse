package com.example.novelverse.domain.model

enum class LibraryCategory(val displayName: String) {
    READING("Reading"),
    PLAN_TO_READ("Plan to Read"),
    COMPLETED("Completed"),
    FAVORITES("Favorites");

    companion object {
        fun fromString(value: String): LibraryCategory {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: READING
        }
    }
}

enum class NovelStatus(val displayName: String) {
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    HIATUS("Hiatus");

    companion object {
        fun fromString(value: String): NovelStatus {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: ONGOING
        }
    }
}

data class Novel(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverUrl: String,
    val coverResId: Int? = null,
    val genres: List<String>,
    val status: NovelStatus = NovelStatus.ONGOING,
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val totalChapters: Int = 0,
    val inLibrary: Boolean = false,
    val libraryCategory: LibraryCategory? = null,
    val lastReadChapterId: String? = null,
    val lastReadChapterTitle: String? = null,
    val lastReadChapterIndex: Int = 0,
    val lastReadPosition: Int = 0, // scroll or paragraph index
    val lastReadTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val sourceUrl: String? = null,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPopular: Boolean = false
)
