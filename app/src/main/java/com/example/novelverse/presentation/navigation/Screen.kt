package com.example.novelverse.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Search : Screen("search")
    object Library : Screen("library")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")

    object NovelDetails : Screen("novel/{novelId}") {
        fun createRoute(novelId: String) = "novel/$novelId"
    }

    object Reader : Screen("reader/{novelId}/{chapterId}") {
        fun createRoute(novelId: String, chapterId: String) = "reader/$novelId/$chapterId"
    }
}
