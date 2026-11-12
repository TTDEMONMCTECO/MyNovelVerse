package com.example.novelverse.data.remote

import com.example.novelverse.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NovelApiService {
    @GET("api/v1/novels/featured")
    suspend fun getFeaturedNovels(): List<NovelDto>

    @GET("api/v1/novels/trending")
    suspend fun getTrendingNovels(): List<NovelDto>

    @GET("api/v1/novels/popular")
    suspend fun getPopularNovels(): List<NovelDto>

    @GET("api/v1/novels/search")
    suspend fun searchNovels(@Query("q") query: String): List<NovelDto>

    @GET("api/v1/novels/{id}")
    suspend fun getNovelDetails(@Path("id") id: String): NovelDto

    @GET("api/v1/novels/{id}/chapters")
    suspend fun getChapters(@Path("id") id: String): List<ChapterDto>

    @GET("api/v1/chapters/{id}")
    suspend fun getChapterContent(@Path("id") id: String): ChapterDto

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponseDto>
}
