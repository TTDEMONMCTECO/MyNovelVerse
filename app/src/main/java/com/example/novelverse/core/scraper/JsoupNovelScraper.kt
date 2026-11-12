package com.example.novelverse.core.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class JsoupNovelScraper {

    suspend fun fetchNovelChapterList(
        novelUrl: String,
        config: ScraperConfig
    ): Result<List<ScrapedChapterItem>> = withContext(Dispatchers.IO) {
        try {
            val doc: Document = Jsoup.connect(novelUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(15000)
                .get()

            val links = doc.select(config.chapterListSelector)
            val chapters = mutableListOf<ScrapedChapterItem>()

            links.forEachIndexed { index, element ->
                val title = element.text().trim()
                val href = element.attr("abs:href")
                if (title.isNotEmpty() && href.isNotEmpty()) {
                    chapters.add(
                        ScrapedChapterItem(
                            title = title,
                            url = href,
                            index = index + 1
                        )
                    )
                }
            }

            Result.success(chapters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChapterContent(
        chapterUrl: String,
        config: ScraperConfig
    ): Result<ScrapedChapterContent> = withContext(Dispatchers.IO) {
        try {
            val doc: Document = Jsoup.connect(chapterUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(15000)
                .get()

            // Extract Chapter Title
            val titleElement = doc.select(config.chapterTitleSelector).first()
            val chapterTitle = titleElement?.text()?.trim() ?: "Chapter"

            // Extract Content
            val contentElement = doc.select(config.chapterContentSelector).first()
                ?: doc.select("article, .entry-content, #content, .content").first()

            val paragraphs = mutableListOf<String>()

            if (contentElement != null) {
                // Remove scripts, ads, style tags
                contentElement.select("script, style, ins, iframe, .ads, .advertisement, button").remove()

                val pElements = contentElement.select("p")
                if (pElements.isNotEmpty()) {
                    pElements.forEach { p ->
                        val text = p.text().trim()
                        if (text.isNotEmpty() && !text.contains("google_ad", ignoreCase = true)) {
                            paragraphs.add(text)
                        }
                    }
                } else {
                    // Fallback to text split by newlines
                    val cleanText = contentElement.wholeText()
                    cleanText.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                        paragraphs.add(it)
                    }
                }
            }

            val fullText = paragraphs.joinToString("\n\n")

            if (paragraphs.isEmpty()) {
                Result.failure(IOException("No chapter text found with selector ${config.chapterContentSelector}"))
            } else {
                Result.success(
                    ScrapedChapterContent(
                        title = chapterTitle,
                        paragraphs = paragraphs,
                        rawText = fullText
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
