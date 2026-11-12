package com.example.novelverse.core.scraper

data class ScraperConfig(
    val name: String = "Web Novel Scraper",
    val baseUrl: String = "https://novelfull.com",
    val titleSelector: String = "h3.title, div.books h3, h1.novel-title",
    val authorSelector: String = "div.info a[href*='author'], span.author, div.author",
    val descriptionSelector: String = "div.desc-text, div.summary, div.novel-description",
    val chapterListSelector: String = "ul.list-chapter li a, div.chapters a, div.chapter-list a",
    val chapterTitleSelector: String = "span.chapter-text, h2, h3",
    val chapterContentSelector: String = "div#chapter-content, div.chapter-c, div.reading-content, div.text-left"
)

data class ScrapedChapterItem(
    val title: String,
    val url: String,
    val index: Int
)

data class ScrapedChapterContent(
    val title: String,
    val paragraphs: List<String>,
    val rawText: String
)
