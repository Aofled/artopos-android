package ru.createsmart.artopos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.createsmart.artopos.core.designsystem.locale.LocaleProvider
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.ui.ArtoposApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val (languageCode, themeConfig) = rememberSettings(uiState)

            LocaleProvider(languageCode = languageCode) {
                ArtoposTheme(themeConfig = themeConfig) {
                    ArtoposApp()
                }
            }
        }
    }
}

@Composable
private fun rememberSettings(uiState: MainActivityUiState): Pair<String, ThemeConfig> {
    return remember(uiState) {
        if (uiState is MainActivityUiState.Success) {
            uiState.settings.languageCode to uiState.settings.themeConfig
        } else {
            "" to ThemeConfig.FOLLOW_SYSTEM
        }
    }
}
