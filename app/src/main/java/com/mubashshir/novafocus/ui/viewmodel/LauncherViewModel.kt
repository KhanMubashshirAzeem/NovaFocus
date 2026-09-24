package com.mubashshir.novafocus.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.data.repository.AppsRepository
import com.mubashshir.novafocus.data.repository.DefaultAppsRepository
import com.mubashshir.novafocus.data.util.IconCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppsRepository = DefaultAppsRepository()
) : AndroidViewModel(application) {

    private val prefs by lazy {
        application.getSharedPreferences("novafocus_launcher_prefs", Context.MODE_PRIVATE)
    }

    private val _uiState = MutableStateFlow(
        LauncherUiState(
            recentSearches = loadRecentSearches(),
            favoriteApps = loadInitialFavorites()
        )
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _hapticEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticEvent: SharedFlow<Unit> = _hapticEvent.asSharedFlow()

    private val searchQueryFlow = MutableStateFlow("")

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())

    private var loadAppsJob: Job? = null

    init {
        startClockUpdates()
        loadInstalledApps()
        observeSearchQuery()
    }

    private fun loadInitialFavorites(): List<AppItem> {
        val context = getApplication<Application>().applicationContext
        val raw = prefs.getString("cached_favorite_apps", "") ?: ""
        if (raw.isNotBlank()) {
            val items = raw.split("\n").mapNotNull { line ->
                val parts = line.split(";")
                if (parts.size >= 2) {
                    val label = parts[0]
                    val pkgName = parts[1]
                    val actName = if (parts.size > 2) parts[2] else ""
                    val cachedIcon = IconCache.getCachedIcon(context, pkgName)
                    AppItem(
                        id = "$pkgName/$actName",
                        label = label,
                        packageName = pkgName,
                        activityName = actName,
                        icon = null,
                        iconBitmap = cachedIcon
                    )
                } else null
            }
            if (items.isNotEmpty()) return items
        }
        // Universal default favorite candidates so the Home screen is never blank on frame 0
        return listOf(
            AppItem(id = "fav_whatsapp", label = "WhatsApp", packageName = "com.whatsapp", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.whatsapp")),
            AppItem(id = "fav_chrome", label = "Chrome", packageName = "com.android.chrome", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.android.chrome")),
            AppItem(id = "fav_camera", label = "Camera", packageName = "com.android.camera", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.android.camera")),
            AppItem(id = "fav_calculator", label = "Calculator", packageName = "com.google.android.calculator", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.google.android.calculator")),
            AppItem(id = "fav_gmail", label = "Gmail", packageName = "com.google.android.gm", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.google.android.gm")),
            AppItem(id = "fav_youtube", label = "YouTube", packageName = "com.google.android.youtube", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.google.android.youtube")),
            AppItem(id = "fav_maps", label = "Maps", packageName = "com.google.android.apps.maps", activityName = "", iconBitmap = IconCache.getCachedIcon(context, "com.google.android.apps.maps"))
        )
    }

    private fun persistFavorites(favorites: List<AppItem>) {
        if (favorites.isEmpty()) return
        val raw = favorites.joinToString("\n") { "${it.label};${it.packageName};${it.activityName}" }
        prefs.edit().putString("cached_favorite_apps", raw).apply()
    }

    private fun loadRecentSearches(): List<String> {
        val raw = prefs.getString("recent_searches", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("\n").filter { it.isNotBlank() }.take(3)
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val updated = current.take(3)
        prefs.edit().putString("recent_searches", updated.joinToString("\n")).apply()
        _uiState.update { it.copy(recentSearches = updated) }
    }

    fun clearRecentSearches() {
        prefs.edit().remove("recent_searches").apply()
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    private fun startClockUpdates() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val now = Date()
                val formattedTime = timeFormat.format(now)
                val formattedDate = dateFormat.format(now)

                _uiState.update { current ->
                    if (current.currentTime != formattedTime || current.currentDate != formattedDate) {
                        current.copy(currentTime = formattedTime, currentDate = formattedDate)
                    } else {
                        current
                    }
                }
                delay(1000L)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce { query ->
                    // 0ms debounce for clearing, 300ms for typing
                    if (query.isEmpty()) 0L else 300L
                }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collectLatest { query ->
                    val results = repository.searchApps(query)
                    _uiState.update { it.copy(searchResults = results) }
                }
        }
    }

    fun loadInstalledApps(forceReload: Boolean = false) {
        if (loadAppsJob?.isActive == true && !forceReload) return

        loadAppsJob = viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            // Phase 1: Fast load smart favorites immediately (typically < 30ms)
            val fastFavorites = repository.getFastFavorites(context)
            if (fastFavorites.isNotEmpty()) {
                persistFavorites(fastFavorites)
                _uiState.update { current ->
                    current.copy(favoriteApps = fastFavorites)
                }
            }

            // Phase 2: Full scan in background for alphabet scrubber and search
            val apps = repository.getInstalledApps(context, forceReload)
            val fullFavorites = repository.getSmartFavorites(context)

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    favoriteApps = if (fullFavorites.isNotEmpty()) fullFavorites else current.favoriteApps,
                    allApps = apps,
                    searchResults = if (current.isSearching) repository.searchApps(current.searchQuery) else emptyList()
                )
            }
        }
    }

    fun onScrubberItemChanged(letter: Char?) {
        val currentLetter = _uiState.value.selectedLetter
        if (currentLetter != letter || !_uiState.value.isScrubbing) {
            _hapticEvent.tryEmit(Unit)
            val filtered = if (letter != null) {
                repository.getAppsStartingWith(letter)
            } else {
                emptyList()
            }
            _uiState.update {
                it.copy(
                    selectedLetter = letter,
                    isScrubbing = true,
                    filteredApps = filtered
                )
            }
        }
    }

    fun onScrubberReleased() {
        _uiState.update {
            it.copy(
                isScrubbing = false
                // Retain selectedLetter and filteredApps so the user stays on that alphabet
            )
        }
    }

    fun returnToHome() {
        _uiState.update {
            it.copy(
                selectedLetter = null,
                isScrubbing = false,
                filteredApps = emptyList()
            )
        }
    }

    fun setSearching(isSearching: Boolean) {
        val initialResults = if (isSearching) repository.searchApps("") else emptyList()
        _uiState.update { current ->
            current.copy(
                isSearching = isSearching,
                searchQuery = if (!isSearching) "" else current.searchQuery,
                searchResults = initialResults
            )
        }
        if (isSearching) {
            searchQueryFlow.value = ""
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun launchApp(context: Context, app: AppItem, isFromSearch: Boolean = false): Boolean {
        if (isFromSearch) {
            addRecentSearch(app.label)
        }
        return repository.launchApp(context, app)
    }
}
