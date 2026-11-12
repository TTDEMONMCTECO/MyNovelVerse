package com.example.novelverse

import com.example.novelverse.core.util.SampleDataGenerator
import com.example.novelverse.domain.model.LibraryCategory
import com.example.novelverse.domain.model.NovelStatus
import org.junit.Assert.*
import org.junit.Test

class NovelVerseUnitTests {

    @Test
    fun testInitialNovelsGeneration() {
        val novels = SampleDataGenerator.getInitialNovels()
        assertTrue("Initial novels should not be empty", novels.isNotEmpty())
        assertEquals(4, novels.size)

        val celestial = novels.first { it.id == "novel_1_celestial" }
        assertEquals("Celestial Ascension: Immortal Sword", celestial.title)
        assertEquals("Li Wei", celestial.author)
        assertTrue(celestial.genres.contains("Cultivation"))
        assertEquals("ONGOING", celestial.status)
    }

    @Test
    fun testInitialChaptersGeneration() {
        val chapters = SampleDataGenerator.getInitialChapters("novel_1_celestial")
        assertEquals(25, chapters.size)
        assertEquals(1, chapters.first().chapterIndex)
        assertEquals(25, chapters.last().chapterIndex)
        assertTrue(chapters.first().content.isNotBlank())
    }

    @Test
    fun testLibraryCategoryParsing() {
        assertEquals(LibraryCategory.READING, LibraryCategory.fromString("READING"))
        assertEquals(LibraryCategory.PLAN_TO_READ, LibraryCategory.fromString("Plan to Read"))
        assertEquals(LibraryCategory.COMPLETED, LibraryCategory.fromString("completed"))
        assertEquals(LibraryCategory.FAVORITES, LibraryCategory.fromString("favorites"))
        assertEquals(LibraryCategory.READING, LibraryCategory.fromString("unknown_category"))
    }

    @Test
    fun testNovelStatusParsing() {
        assertEquals(NovelStatus.ONGOING, NovelStatus.fromString("ONGOING"))
        assertEquals(NovelStatus.COMPLETED, NovelStatus.fromString("Completed"))
        assertEquals(NovelStatus.HIATUS, NovelStatus.fromString("hiatus"))
    }
}
