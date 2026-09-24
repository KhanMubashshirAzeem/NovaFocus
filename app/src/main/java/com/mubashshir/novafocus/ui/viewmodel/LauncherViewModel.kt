package com.mubashshir.novafocus.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.data.repository.AppsRepository
import com.mubashshir.novafocus.data.repository.DefaultAppsRepository
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AppsRepository = DefaultAppsRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _hapticEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticEvent: SharedFlow<Unit> = _hapticEvent.asSharedFlow()

    private val searchQueryFlow = MutableStateFlow("")

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())

    init {
        startClockUpdates()
        loadInstalledApps()
        observeSearchQuery()
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val context = getApplication<Application>().applicationContext
            val apps = repository.getInstalledApps(context, forceReload)
            val favorites = repository.getSmartFavorites(context)

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    favoriteApps = favorites,
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

    fun launchApp(context: Context, app: AppItem): Boolean {
        return repository.launchApp(context, app)
    }
}
