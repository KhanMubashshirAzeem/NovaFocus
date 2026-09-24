package com.mubashshir.novafocus

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mubashshir.novafocus.ui.screens.HomeScreen
import com.mubashshir.novafocus.ui.theme.NovaFocusTheme
import com.mubashshir.novafocus.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        // Enforce dark UI mode for the entire activity context so soft keyboards (GBoard/IME) render in Dark Mode
        val config = Configuration(newBase.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this) {
            val state = viewModel.uiState.value
            when {
                state.isSearching -> viewModel.setSearching(false)
                state.selectedLetter != null -> viewModel.returnToHome()
                else -> moveTaskToBack(true)
            }
        }

        setContent {
            NovaFocusTheme(darkTheme = true) {
                HomeScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed apps list if any app was installed/uninstalled
        viewModel.loadInstalledApps(forceReload = false)
    }
}