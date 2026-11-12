package com.example.novelverse.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "author") val author: String,
    @Json(name = "description") val description: String,
    @Json(name = "cover_url") val coverUrl: String,
    @Json(name = "genres") val genres: List<String>,
    @Json(name = "status") val status: String? = "ONGOING",
    @Json(name = "rating") val rating: Float = 4.8f,
    @Json(name = "review_count") val reviewCount: Int = 100,
    @Json(name = "total_chapters") val totalChapters: Int = 0,
    @Json(name = "source_url") val sourceUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ChapterDto(
    @Json(name = "id") val id: String,
    @Json(name = "novel_id") val novelId: String,
    @Json(name = "chapter_index") val index: Int,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_in") val expiresIn: Long
)
