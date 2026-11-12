package com.example.novelverse.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.novelverse.NovelVerseApplication
import com.example.novelverse.presentation.details.NovelDetailsScreen
import com.example.novelverse.presentation.details.NovelDetailsViewModel
import com.example.novelverse.presentation.discover.DiscoverScreen
import com.example.novelverse.presentation.discover.DiscoverViewModel
import com.example.novelverse.presentation.downloads.DownloadsScreen
import com.example.novelverse.presentation.downloads.DownloadsViewModel
import com.example.novelverse.presentation.home.HomeScreen
import com.example.novelverse.presentation.home.HomeViewModel
import com.example.novelverse.presentation.library.LibraryScreen
import com.example.novelverse.presentation.library.LibraryViewModel
import com.example.novelverse.presentation.reader.ReaderScreen
import com.example.novelverse.presentation.reader.ReaderViewModel
import com.example.novelverse.presentation.search.SearchScreen
import com.example.novelverse.presentation.search.SearchViewModel
import com.example.novelverse.presentation.settings.SettingsScreen
import com.example.novelverse.presentation.settings.SettingsViewModel

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val BottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Default.Home, Icons.Outlined.Home),
    BottomNavItem("Discover", Screen.Discover.route, Icons.Default.Explore, Icons.Outlined.Explore),
    BottomNavItem("Library", Screen.Library.route, Icons.Default.Bookmark, Icons.Outlined.BookmarkBorder),
    BottomNavItem("Settings", Screen.Settings.route, Icons.Default.Settings, Icons.Outlined.Settings)
)

@Composable
fun NovelVerseNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as NovelVerseApplication
    val container = context.container

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = BottomNavItems.any { it.route == currentRoute }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation Rail on Expanded/Tablet screens
            if (isExpanded && isTopLevelDestination) {
            NavigationRail(
                modifier = Modifier.testTag("tablet_nav_rail"),
                header = {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "NovelVerse",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            ) {
                BottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }

        Scaffold(
            bottomBar = {
                // Bottom Navigation Bar on Mobile / Compact screens
                if (!isExpanded && isTopLevelDestination) {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        BottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) },
                                modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Home Screen
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(container.getNovelsUseCase, container.libraryUseCases)
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNovelClick = { novelId ->
                            navController.navigate(Screen.NovelDetails.createRoute(novelId))
                        },
                        onContinueReadingClick = { novelId, chapterId ->
                            navController.navigate(Screen.Reader.createRoute(novelId, chapterId))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onDownloadsClick = {
                            navController.navigate(Screen.Downloads.route)
                        }
                    )
                }

                // Discover Screen
                composable(Screen.Discover.route) {
                    val discoverViewModel: DiscoverViewModel = viewModel(
                        factory = DiscoverViewModel.Factory(container.getNovelsUseCase)
                    )
                    DiscoverScreen(
                        viewModel = discoverViewModel,
                        onNovelClick = { novelId ->
                            navController.navigate(Screen.NovelDetails.createRoute(novelId))
                        }
                    )
                }

                // Search Screen
                composable(Screen.Search.route) {
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = SearchViewModel.Factory(container.getNovelsUseCase, container.searchRepository)
                    )
                    SearchScreen(
                        viewModel = searchViewModel,
                        onNovelClick = { novelId ->
                            navController.navigate(Screen.NovelDetails.createRoute(novelId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Library Screen
                composable(Screen.Library.route) {
                    val libraryViewModel: LibraryViewModel = viewModel(
                        factory = LibraryViewModel.Factory(container.libraryUseCases)
                    )
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onNovelClick = { novelId ->
                            navController.navigate(Screen.NovelDetails.createRoute(novelId))
                        },
                        onResumeReading = { novelId, chapterId ->
                            navController.navigate(Screen.Reader.createRoute(novelId, chapterId))
                        },
                        onExploreClick = {
                            navController.navigate(Screen.Discover.route)
                        }
                    )
                }

                // Downloads Screen
                composable(Screen.Downloads.route) {
                    val downloadsViewModel: DownloadsViewModel = viewModel(
                        factory = DownloadsViewModel.Factory(container.downloadUseCases)
                    )
                    DownloadsScreen(
                        viewModel = downloadsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Settings Screen
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.Factory(container.preferencesManager, container.scraper)
                    )
                    SettingsScreen(viewModel = settingsViewModel)
                }

                // Novel Details Screen
                composable(
                    route = Screen.NovelDetails.route,
                    arguments = listOf(navArgument("novelId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val novelId = backStackEntry.arguments?.getString("novelId") ?: ""
                    val detailsViewModel: NovelDetailsViewModel = viewModel(
                        key = novelId,
                        factory = NovelDetailsViewModel.Factory(
                            novelId,
                            container.getNovelDetailsUseCase,
                            container.libraryUseCases,
                            container.bookmarkUseCases,
                            container.downloadUseCases
                        )
                    )
                    NovelDetailsScreen(
                        viewModel = detailsViewModel,
                        onChapterClick = { nid, cid ->
                            navController.navigate(Screen.Reader.createRoute(nid, cid))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Advanced Reader Screen
                composable(
                    route = Screen.Reader.route,
                    arguments = listOf(
                        navArgument("novelId") { type = NavType.StringType },
                        navArgument("chapterId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val novelId = backStackEntry.arguments?.getString("novelId") ?: ""
                    val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                    val readerViewModel: ReaderViewModel = viewModel(
                        key = "$novelId-$chapterId",
                        factory = ReaderViewModel.Factory(
                            novelId,
                            chapterId,
                            container.getChapterContentUseCase,
                            container.getNovelDetailsUseCase,
                            container.bookmarkUseCases,
                            container.preferencesManager
                        )
                    )
                    ReaderScreen(
                        viewModel = readerViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}

