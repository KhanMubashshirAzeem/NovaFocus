package com.mubashshir.novafocus.ui.viewmodel

import com.mubashshir.novafocus.data.model.AppItem

/**
 * UI State for the NovaFocus Launcher.
 */
data class LauncherUiState(
    val isLoading: Boolean = true,
    val currentTime: String = "",
    val currentDate: String = "",
    val favoriteApps: List<AppItem> = emptyList(),
    val selectedLetter: Char? = null,
    val isScrubbing: Boolean = false,
    val filteredApps: List<AppItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<AppItem> = emptyList()
) {
    val isViewingLetter: Boolean
        get() = selectedLetter != null
}
