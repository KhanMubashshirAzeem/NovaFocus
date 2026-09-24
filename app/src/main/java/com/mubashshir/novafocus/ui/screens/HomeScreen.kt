package com.mubashshir.novafocus.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.mubashshir.novafocus.ui.components.AlphabetScrubber
import com.mubashshir.novafocus.ui.components.FavoritesSection
import com.mubashshir.novafocus.ui.components.FilteredAppsSection
import com.mubashshir.novafocus.ui.components.SearchOverlay
import com.mubashshir.novafocus.ui.theme.BackgroundDark
import com.mubashshir.novafocus.ui.viewmodel.LauncherViewModel

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!uiState.isSearching && uiState.activeScrubberLetter == null) {
                        Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -30f) {
                                    viewModel.setSearching(true)
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            when {
                uiState.isSearching -> {
                    SearchOverlay(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        results = uiState.searchResults,
                        onAppClick = { app -> viewModel.launchApp(context, app) },
                        onDismiss = { viewModel.setSearching(false) }
                    )
                }

                uiState.activeScrubberLetter != null -> {
                    FilteredAppsSection(
                        letter = uiState.activeScrubberLetter!!,
                        apps = uiState.filteredApps,
                        onAppClick = { app -> viewModel.launchApp(context, app) }
                    )
                }

                else -> {
                    FavoritesSection(
                        time = uiState.currentTime,
                        date = uiState.currentDate,
                        favorites = uiState.favoriteApps,
                        onAppClick = { app -> viewModel.launchApp(context, app) }
                    )
                }
            }
        }

        // Pinned Alphabet Scrubber on the Right Edge (hidden during search)
        if (!uiState.isSearching) {
            AlphabetScrubber(
                activeLetter = uiState.activeScrubberLetter,
                onLetterSelected = { letter ->
                    viewModel.onScrubberItemChanged(letter)
                },
                onRelease = {
                    viewModel.onScrubberReleased()
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
