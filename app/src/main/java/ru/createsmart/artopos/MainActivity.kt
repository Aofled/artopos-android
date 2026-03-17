package ru.createsmart.artopos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.ui.ArtoposApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val currentState = uiState

            val config = if (currentState is MainActivityUiState.Success) {
                currentState.themeConfig
            } else {
                ThemeConfig.FOLLOW_SYSTEM
            }

            ArtoposTheme(themeConfig = config) {
                ArtoposApp()
            }
        }
    }
}
