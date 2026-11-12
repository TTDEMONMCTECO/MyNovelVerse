package com.example.novelverse.data.repository

import com.example.novelverse.core.network.NetworkResult
import com.example.novelverse.core.scraper.JsoupNovelScraper
import com.example.novelverse.core.scraper.ScraperConfig
import com.example.novelverse.core.util.SampleDataGenerator
import com.example.novelverse.data.local.dao.ChapterDao
import com.example.novelverse.data.local.dao.NovelDao
import com.example.novelverse.data.local.entity.ChapterEntity
import com.example.novelverse.data.local.entity.NovelEntity
import com.example.novelverse.data.remote.NovelApiService
import com.example.novelverse.domain.model.Novel
import com.example.novelverse.domain.repository.NovelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class NovelRepositoryImpl(
    private val novelDao: NovelDao,
    private val chapterDao: ChapterDao,
    private val apiService: NovelApiService,
    private val scraper: JsoupNovelScraper,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : NovelRepository {

    init {
        // Pre-populate sample novels and chapters on first launch if empty
        scope.launch {
            val existing = novelDao.getNovelByIdOnce("novel_1_celestial")
            if (existing == null) {
                val novels = SampleDataGenerator.getInitialNovels()
                novelDao.insertNovels(novels)
                novels.forEach { novel ->
                    val chapters = SampleDataGenerator.getInitialChapters(novel.id)
                    chapterDao.insertChapters(chapters)
                }
            }
        }
    }

    override fun getAllNovels(): Flow<List<Novel>> =
        novelDao.getAllNovels().map { list -> list.map { it.toDomain() } }

    override fun getFeaturedNovels(): Flow<List<Novel>> =
        novelDao.getFeaturedNovels().map { list -> list.map { it.toDomain() } }

    override fun getTrendingNovels(): Flow<List<Novel>> =
        novelDao.getTrendingNovels().map { list -> list.map { it.toDomain() } }

    override fun getPopularNovels(): Flow<List<Novel>> =
        novelDao.getPopularNovels().map { list -> list.map { it.toDomain() } }

    override fun getLatestNovels(): Flow<List<Novel>> =
        novelDao.getLatestNovels().map { list -> list.map { it.toDomain() } }

    override fun getNovelById(id: String): Flow<Novel?> =
        novelDao.getNovelById(id).map { it?.toDomain() }

    override suspend fun getNovelByIdOnce(id: String): Novel? =
        novelDao.getNovelByIdOnce(id)?.toDomain()

    override fun searchNovels(query: String): Flow<List<Novel>> =
        novelDao.searchNovels(query).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshNovels(): NetworkResult<Unit> {
        return try {
            // Check if remote API is available; otherwise fallback to healthy local state
            val remoteNovels = try {
                apiService.getFeaturedNovels()
            } catch (e: Exception) {
                null
            }

            if (remoteNovels != null && remoteNovels.isNotEmpty()) {
                val entities = remoteNovels.map { dto ->
                    NovelEntity(
                        id = dto.id,
                        title = dto.title,
                        author = dto.author,
                        description = dto.description,
                        coverUrl = dto.coverUrl,
                        genres = dto.genres.joinToString(","),
                        status = dto.status ?: "ONGOING",
                        rating = dto.rating,
                        reviewCount = dto.reviewCount,
                        totalChapters = dto.totalChapters,
                        sourceUrl = dto.sourceUrl
                    )
                }
                novelDao.insertNovels(entities)
            }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to refresh: ${e.localizedMessage ?: "Unknown error"}", cause = e)
        }
    }

    override suspend fun syncNovelFromWeb(url: String): NetworkResult<Novel> {
        return try {
            val config = ScraperConfig()
            val chaptersResult = scraper.fetchNovelChapterList(url, config)
            if (chaptersResult.isSuccess) {
                val scrapedChapters = chaptersResult.getOrThrow()
                val novelId = "web_" + UUID.randomUUID().toString().take(8)
                val newNovel = NovelEntity(
                    id = novelId,
                    title = "Imported Web Novel",
                    author = "Web Author",
                    description = "Imported from $url via NovelVerse Jsoup Web Engine.",
                    coverUrl = "",
                    genres = "Web Novel,Imported,Fantasy",
                    status = "ONGOING",
                    rating = 4.5f,
                    reviewCount = 10,
                    totalChapters = scrapedChapters.size,
                    sourceUrl = url,
                    inLibrary = true,
                    libraryCategory = "READING"
                )
                novelDao.insertNovel(newNovel)

                val chapterEntities = scrapedChapters.map { item ->
                    ChapterEntity(
                        id = "${novelId}_ch_${item.index}",
                        novelId = novelId,
                        chapterIndex = item.index,
                        title = item.title,
                        content = "",
                        url = item.url,
                        releaseDate = "Imported"
                    )
                }
                chapterDao.insertChapters(chapterEntities)
                NetworkResult.Success(newNovel.toDomain())
            } else {
                NetworkResult.Error("Scraper error: ${chaptersResult.exceptionOrNull()?.localizedMessage}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed to import novel: ${e.localizedMessage}")
        }
    }
}
