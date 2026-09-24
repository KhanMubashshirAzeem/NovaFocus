package com.mubashshir.novafocus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
        if (uiState.isSearching) {
            // Full screen search overlay
            SearchOverlay(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                results = uiState.searchResults,
                onAppClick = { app -> viewModel.launchApp(context, app) },
                onDismiss = { viewModel.setSearching(false) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Left 70% width: Content Area (Favorites / Filtered Alphabet list)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.70f)
                    .align(Alignment.CenterStart)
                    .then(
                        if (uiState.selectedLetter == null) {
                            // Detect swipe from bottom to up to open search
                            Modifier.pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var triggered = false
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) break

                                        val deltaY = change.position.y - down.position.y
                                        if (deltaY < -35f && !triggered) {
                                            triggered = true
                                            change.consume()
                                            viewModel.setSearching(true)
                                            break
                                        }
                                    }
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (uiState.selectedLetter != null) {
                    FilteredAppsSection(
                        letter = uiState.selectedLetter!!,
                        apps = uiState.filteredApps,
                        onAppClick = { app -> viewModel.launchApp(context, app) },
                        onBackToHome = { viewModel.returnToHome() }
                    )
                } else {
                    FavoritesSection(
                        time = uiState.currentTime,
                        date = uiState.currentDate,
                        favorites = uiState.favoriteApps,
                        onAppClick = { app -> viewModel.launchApp(context, app) }
                    )
                }
            }

            // Right 30% width: Dedicated Alphabet Scrubber Area
            AlphabetScrubber(
                selectedLetter = uiState.selectedLetter,
                onLetterSelected = { letter ->
                    viewModel.onScrubberItemChanged(letter)
                },
                onRelease = {
                    viewModel.onScrubberReleased()
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.30f)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}
