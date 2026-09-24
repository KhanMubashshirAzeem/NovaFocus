package com.mubashshir.novafocus
 
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.mubashshir.novafocus.ui.screens.HomeScreen
import com.mubashshir.novafocus.ui.theme.NovaFocusTheme
import com.mubashshir.novafocus.ui.viewmodel.LauncherViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        // Enforce dark UI mode for the entire activity context so soft keyboards (GBoard/IME) render in Dark Mode
        val config = Configuration(newBase.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        val config = Configuration(overrideConfiguration ?: resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        }
        super.applyOverrideConfiguration(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Night Mode globally for this app so soft keyboards (GBoard/IME) render in dark theme
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
        }

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        onBackPressedDispatcher.addCallback(this) {
            val state = viewModel.uiState.value
            when {
                state.isSearching -> viewModel.setSearching(false)
                state.selectedLetter != null -> viewModel.returnToHome()
                else -> {
                    // NovaFocus is the launcher root; stay on home screen
                }
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