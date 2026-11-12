package com.example.novelverse.core.util

import com.example.R
import com.example.novelverse.data.local.entity.ChapterEntity
import com.example.novelverse.data.local.entity.NovelEntity

object SampleDataGenerator {

    fun getInitialNovels(): List<NovelEntity> {
        return listOf(
            NovelEntity(
                id = "novel_1_celestial",
                title = "Celestial Ascension: Immortal Sword",
                author = "Li Wei",
                description = "In the boundless Nine Heavens realm, mortals cultivate Qi to defy the celestial tribulations. Chen Ye, a discarded outer disciple of the Azure Cloud Sect, stumbles upon the ancient Primeval Sovereign Sword buried in the Thunderfall Ravine. Awakening the dormant Celestial Sword Core within his dantian, he embarks on an unyielding ascent through nine imperial domains, slaying celestial deities and unraveling the mystery of the shattered Heavens.",
                coverUrl = "",
                coverResId = R.drawable.cover_celestial,
                genres = "Fantasy,Cultivation,Xianxia,Action,Martial Arts",
                status = "ONGOING",
                rating = 4.9f,
                reviewCount = 2840,
                totalChapters = 25,
                inLibrary = true,
                libraryCategory = "READING",
                lastReadChapterId = "novel_1_ch_1",
                lastReadChapterTitle = "Chapter 1: The Broken Meridian",
                lastReadChapterIndex = 1,
                lastReadPosition = 0,
                lastReadTimestamp = System.currentTimeMillis() - 3600000,
                isFavorite = true,
                isFeatured = true,
                isTrending = true,
                isPopular = true
            ),
            NovelEntity(
                id = "novel_2_neural",
                title = "Neural Breach: Cyber Shadows",
                author = "Kairo",
                description = "Neo-Kyoto, 2184. Mega-corporation Arasaka Neural Dynamics controls every memory synapse downloaded into the citizens' cerebral ports. Ren, a rogue netrunner operating in the neon shadows of the Lower Docks, intercepts an encrypted military data packet containing an autonomous god-tier AI capable of rewriting physical reality. Hunted by cyborg hitmen and corporate drones, Ren must dive into the darkest layers of the Deep Grid.",
                coverUrl = "",
                coverResId = R.drawable.cover_cyber,
                genres = "Sci-Fi,Cyberpunk,Thriller,Dystopian,Action",
                status = "ONGOING",
                rating = 4.8f,
                reviewCount = 1920,
                totalChapters = 20,
                inLibrary = true,
                libraryCategory = "PLAN_TO_READ",
                lastReadChapterId = null,
                lastReadChapterTitle = null,
                lastReadChapterIndex = 0,
                lastReadPosition = 0,
                lastReadTimestamp = 0L,
                isFavorite = false,
                isFeatured = true,
                isTrending = true,
                isPopular = false
            ),
            NovelEntity(
                id = "novel_3_arcane",
                title = "The Silent Scriptorium",
                author = "Elara Vance",
                description = "Deep beneath the Obsidian Citadel lies the Grand Archive of Aloria—a labyrinth of millions of living books that whisper forbidden truths to those reckless enough to listen. Corin, an apprentice scribe burdened with the rare talent to read runic thought-forms, discovers that the ancient arch-mages did not die: they bound their immortal consciousness into the library's cursed forbidden wing.",
                coverUrl = "",
                coverResId = R.drawable.cover_arcane,
                genres = "Fantasy,Mystery,Magic Academy,Adventure",
                status = "COMPLETED",
                rating = 4.7f,
                reviewCount = 1450,
                totalChapters = 22,
                inLibrary = true,
                libraryCategory = "COMPLETED",
                lastReadChapterId = "novel_3_ch_22",
                lastReadChapterTitle = "Chapter 22: The Master of Tomes",
                lastReadChapterIndex = 22,
                lastReadPosition = 0,
                lastReadTimestamp = System.currentTimeMillis() - 86400000,
                isFavorite = true,
                isFeatured = false,
                isTrending = true,
                isPopular = true
            ),
            NovelEntity(
                id = "novel_4_shadow",
                title = "Shadow Monarch: System Apocalypse",
                author = "Jonathan Drake",
                description = "When blue dungeon rifts tore open across Earth's major cities, the Cosmic Arbiter System awakened within humanity. While most awakeners received elemental or warrior classes, Lukas received an anomalous F-Rank necromancy variant: 'Ruler of the Eternal Abyss'. Dismissed as a suicidal liability, Lukas enters the abyssal gate alone and unlocks the power to extract fallen monarchs as his eternal legion.",
                coverUrl = "",
                coverResId = R.drawable.cover_shadow,
                genres = "LitRPG,Fantasy,System,Reincarnation,Action",
                status = "ONGOING",
                rating = 4.95f,
                reviewCount = 4310,
                totalChapters = 30,
                inLibrary = true,
                libraryCategory = "FAVORITES",
                lastReadChapterId = "novel_4_ch_5",
                lastReadChapterTitle = "Chapter 5: Extract the Monarch's Shadow",
                lastReadChapterIndex = 5,
                lastReadPosition = 0,
                lastReadTimestamp = System.currentTimeMillis() - 7200000,
                isFavorite = true,
                isFeatured = true,
                isTrending = true,
                isPopular = true
            )
        )
    }

    fun getInitialChapters(novelId: String): List<ChapterEntity> {
        val count = when (novelId) {
            "novel_1_celestial" -> 25
            "novel_2_neural" -> 20
            "novel_3_arcane" -> 22
            "novel_4_shadow" -> 30
            else -> 15
        }

        val novelTitle = when (novelId) {
            "novel_1_celestial" -> "Celestial Ascension"
            "novel_2_neural" -> "Neural Breach"
            "novel_3_arcane" -> "The Silent Scriptorium"
            "novel_4_shadow" -> "Shadow Monarch"
            else -> "Novel"
        }

        return (1..count).map { idx ->
            ChapterEntity(
                id = "${novelId}_ch_$idx",
                novelId = novelId,
                chapterIndex = idx,
                title = getChapterTitle(novelId, idx),
                content = generateChapterStoryText(novelId, idx, novelTitle),
                url = "https://novelverse.example.com/novel/$novelId/chapter/$idx",
                releaseDate = "2026-11-${String.format("%02d", (idx % 28) + 1)}",
                isRead = (idx <= 3),
                isDownloaded = (idx <= 5),
                isBookmarked = (idx == 1)
            )
        }
    }

    private fun getChapterTitle(novelId: String, index: Int): String {
        return when (novelId) {
            "novel_1_celestial" -> when (index) {
                1 -> "Chapter 1: The Broken Meridian"
                2 -> "Chapter 2: Thunderfall Ravine"
                3 -> "Chapter 3: The Sovereign Core Awakens"
                4 -> "Chapter 4: Slaying the Iron Fang Wolf"
                5 -> "Chapter 5: Return to Azure Cloud"
                6 -> "Chapter 6: Duel at the Martial Pavilion"
                7 -> "Chapter 7: Crimson Flame Lotus"
                8 -> "Chapter 8: Elder Yun's Ultimatum"
                else -> "Chapter $index: The Heavenly Tribulation Arc Part ${index - 8}"
            }
            "novel_2_neural" -> when (index) {
                1 -> "Chapter 1: Rain Over District 7"
                2 -> "Chapter 2: The Quantum Cipher"
                3 -> "Chapter 3: Flatline Protocol"
                4 -> "Chapter 4: Chrome and Neon"
                5 -> "Chapter 5: Ghost in the Neural Grid"
                else -> "Chapter $index: Syndicate Infiltration Phase ${index - 5}"
            }
            "novel_3_arcane" -> when (index) {
                1 -> "Chapter 1: The Whispering Stacks"
                2 -> "Chapter 2: Ink of the Wyrm"
                3 -> "Chapter 3: The Sealed Codex"
                4 -> "Chapter 4: Midnight at the Archway"
                else -> "Chapter $index: The Forgotten Runes Part ${index - 4}"
            }
            "novel_4_shadow" -> when (index) {
                1 -> "Chapter 1: Awakening in the F-Rank Catacomb"
                2 -> "Chapter 2: The Cosmic System Ping"
                3 -> "Chapter 3: Extraction: Rise, Shadow Knight!"
                4 -> "Chapter 4: Solo Raid in the Sub-Level"
                5 -> "Chapter 5: Extract the Monarch's Shadow"
                else -> "Chapter $index: Dungeon Conquest Floor ${index + 10}"
            }
            else -> "Chapter $index: The Journey Begins"
        }
    }

    private fun generateChapterStoryText(novelId: String, index: Int, novelTitle: String): String {
        return """
            The cold morning mist rolled heavily over the mountain cliffs, carrying with it the bitter chill of late autumn. In the quiet solitude before dawn, every breath condensed into pale plumes of vapor against the steel-grey horizon.
            
            "The path we walk is never chosen in comfort," whispered the solitary figure, his eyes scanning the endless expanse beneath him. "It is forged in relentless adversity, step by step, tribulation by tribulation."
            
            A sudden tremor resonated through the air. The surrounding atmosphere hummed with sudden, concentrated energy—a sharp crackle that sent ripples across the damp earth. Trees swayed without wind, and the birds of the canopy fell instantly into deafening silence.
            
            He tightened his grip upon the weapon strapped securely to his side. The metallic sheath was worn and etched with ancient markings, scars from battles fought across long-forgotten battlefields. Deep within the core, a soft golden pulse began to throb, synchronizing with the rapid beat of his heart.
            
            [SYSTEM ALERT: Spatial resonance detected within 500 meters.]
            [Resonance Level: Grade IV — High Threat Level.]
            
            "Right on schedule," a faint smirk tugged at the corner of his lips. There was no hesitation in his stance; instead, his posture settled into a stance honed through thousands of grueling hours of practice.
            
            From the fog emerged a shape that defied natural order. Its silhouette towered eight cubits tall, shrouded in roiling violet miasma. Twin eyes burned like dying embers from within the shadowy maw, locking intently onto the solitary warrior standing on the cliff edge.
            
            "You dare trespass upon the sacred domain of the Sovereign?" the beast's voice rumbled like grinding tectonic plates, reverberating deep inside his chest.
            
            "I do not merely trespass," he replied, drawing his blade with an effortless sweep that cleaved the descending fog in two clean halves. A brilliant arc of pure incandescent light flashed across the mountain crest. "I have arrived to claim what was stolen ten thousand years ago."
            
            With a thunderous roar, the behemoth lunged forward, clawed talons rending the air with supersonic fury. But before the beast could close the distance, the space around them warped. Runes ignited in concentric circles beneath their feet, blazing with primordial brilliance that banished the morning darkness.
            
            The battle for the destiny of $novelTitle had truly begun.
            
            Every strike, every parry echoed across the valley like claps of thunder. Dust and shattered rock exploded into the air as the two forces collided again and again. Yet despite the ferocious onslaught, his breathing remained steady, his focus razor-sharp. He had prepared for this moment for years, and defeat was not an option he would ever entertain.
            
            As the final echo of the confrontation subsided into the quiet morning, only one stood atop the shattered stone. Breathing deeply, he sheathed his weapon and turned his gaze toward the towering peaks in the far distance, where the celestial palace awaited.
        """.trimIndent()
    }
}
