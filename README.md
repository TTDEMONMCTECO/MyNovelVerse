# NovelVerse - Professional Android Novel Reading Application

NovelVerse is a complete, modern, high-performance novel reading application built with **Jetpack Compose**, **Kotlin**, **Clean Architecture (MVVM)**, **Room Database**, **Retrofit & OkHttp**, **Jsoup Web Scraping Engine**, and **WorkManager**.

---

## 🚀 Key Features

### 1. 📖 Discovery & Home
- **Hero Carousel Banner:** Featured novels with high-resolution atmospheric book covers.
- **Continue Reading Card:** Instant one-tap resume to your exact last-read chapter and progress.
- **Trending & Popular Charts:** Dynamic ranking badges (#1, #2, #3) and rating reviews.
- **Latest Updates:** Real-time stream of recently added and updated chapters.
- **Discover by Genre & Status:** Filter by Fantasy, Cultivation, Xianxia, Sci-Fi, Cyberpunk, Mystery, System, LitRPG, Action, and Ongoing/Completed.
- **Web Novel URL Importer:** Uses Jsoup with configurable CSS selectors to import any authorized online web novel.

### 2. 🔍 Advanced Search
- Real-time debounced search by novel title, author name, or genre tags.
- Persistent search history with chip tags, single-item deletion, and clear all.

### 3. 📚 Novel Details
- High-res cover art, author bio, star rating, total chapter counts, and status indicators.
- Collapsible synopsis with read more/less.
- Tabs for Chapters and Bookmarks.
- Chapter sorting (Ascending / Descending order).
- Download All button for offline storage.

### 4. 👓 Advanced Immersive Reader
- Fullscreen distraction-free reading surface.
- **6 Reading Themes:**
  - **White:** Crisp clean paper
  - **Sepia:** Classic warm book tint
  - **Cream:** Soft low-strain background
  - **Night:** Deep slate reading mode
  - **AMOLED:** True pitch black for OLED battery saving
  - **Forest:** Calming emerald tint
- **Full Typography Control:**
  - Font Size adjustments (12sp to 32sp)
  - Font Family switcher (Serif, Sans-Serif, Monospace, Cursive)
  - Line height multipliers (Compact 1.3x, Normal 1.6x, Relaxed 2.0x)
  - Paragraph spacing options
- **Software Dimmer Overlay:** In-app brightness control (20% to 100%).
- **Quick Table of Contents:** Bottom sheet allowing instant jumps between chapters.
- **Bookmarking:** Highlight and save excerpts with personal notes.
- **Progress Tracking:** Automatic paragraph and percentage scroll persistence.

### 5. 🏷️ Library Management
- Organized tabs: **Reading**, **Plan to Read**, **Completed**, **Favorites**, and **Reading History**.
- Track reading progress per book (e.g. "Ch. 5 / 30").
- One-tap quick play button on novel cards.

### 6. ⬇️ Download Manager & Offline Reading
- WorkManager background worker downloading chapters.
- Download status tracking: Pending, Downloading, Completed, Failed.
- Storage stats and bulk delete tools.
- True offline-first capability: read all saved novels without internet connection.

### 7. ⚙️ Settings & Customization
- App Theme Mode: System Default, Light, or Dark.
- Accent Color: Indigo, Amber, Emerald, Crimson, Rose.
- Scraper Selector Configuration: test custom CSS selectors live with URL testing.
- Notification toggles for new chapters.

---

## 🏗️ Architecture & Tech Stack

```
com.example.novelverse/
├── core/
│   ├── datastore/       # PreferencesManager for auth tokens, reader prefs, settings
│   ├── network/         # HeaderInterceptor, AuthInterceptor (with 401 token refresh), NetworkResult
│   ├── scraper/         # JsoupNovelScraper with configurable CSS selectors
│   ├── theme/           # Material 3 Color presets, typography, and NovelVerseTheme
│   └── util/            # SampleDataGenerator (rich pre-populated fantasy & sci-fi novels)
├── data/
│   ├── local/
│   │   ├── NovelVerseDatabase (Room DB, version 1)
│   │   ├── dao/         # NovelDao, ChapterDao, BookmarkDao, SearchHistoryDao, DownloadDao
│   │   └── entity/      # NovelEntity, ChapterEntity, BookmarkEntity, SearchHistoryEntity, DownloadEntity
│   ├── remote/
│   │   ├── NovelApiService (Retrofit REST service)
│   │   └── dto/         # Moshi JSON DTOs
│   └── repository/      # NovelRepositoryImpl, ReaderRepositoryImpl, LibraryRepositoryImpl, etc.
├── domain/
│   ├── model/           # Novel, Chapter, Bookmark, DownloadItem, LibraryCategory, NovelStatus
│   ├── repository/      # Repository interfaces (Clean Architecture boundaries)
│   └── usecase/         # GetNovelsUseCase, GetNovelDetailsUseCase, GetChapterContentUseCase, LibraryUseCases, etc.
├── di/
│   └── AppContainer.kt  # Clean Architecture dependency injection container
├── worker/
│   └── DownloadChapterWorker.kt # WorkManager background chapter downloader
└── presentation/
    ├── navigation/      # NavGraph with adaptive NavigationRail (tablet) & NavigationBar (phone)
    ├── components/      # NovelCoverImage, NovelCard, RatingBadge, ChapterListItem, etc.
    ├── home/            # HomeScreen & HomeViewModel
    ├── discover/        # DiscoverScreen & DiscoverViewModel
    ├── search/          # SearchScreen & SearchViewModel
    ├── details/         # NovelDetailsScreen & NovelDetailsViewModel
    ├── reader/          # ReaderScreen, ReaderViewModel, ReaderControls
    ├── library/         # LibraryScreen & LibraryViewModel
    ├── downloads/       # DownloadsScreen & DownloadsViewModel
    └── settings/        # SettingsScreen & SettingsViewModel
```

---

## 🛠️ Build & Run Instructions

1. **Prerequisites:** Android Studio Ladybug / Meerkat or later, JDK 17 / 21, Android SDK 36.
2. **Open Project:** Open this directory in Android Studio.
3. **Run Gradle Build:**
   ```bash
   gradle assembleDebug
   ```
4. **Run Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```
