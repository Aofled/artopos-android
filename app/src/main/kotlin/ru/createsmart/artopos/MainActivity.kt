package ru.createsmart.artopos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.locale.LocaleProvider
import ru.createsmart.artopos.ui.ArtoposApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Block the splash screen from disappearing until the data is loaded.
        // The UI thread will keep the logo screen visible until uiState == Loading.
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainActivityUiState.Loading
        }
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val currentState = uiState

            // If the settings aren't read from disk, we don't draw anything.
            // The system splash screen will hide this blank screen.
            if (currentState !is MainActivityUiState.Success) {
                return@setContent
            }

            // If we know the user's language and topic exactly
            val settings = currentState.settings
            val languageCode = settings.languageCode
            val themeConfig = settings.themeConfig

            LocaleProvider(languageCode = languageCode) {
                ArtoposTheme(themeConfig = themeConfig) {
                    ArtoposApp()
                }
            }
        }
    }
}
