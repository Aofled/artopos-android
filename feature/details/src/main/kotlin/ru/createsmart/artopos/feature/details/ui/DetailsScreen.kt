package ru.createsmart.artopos.feature.details.ui

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.ui.components.BackButton
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.details.ArtworkDetailUiState
import ru.createsmart.artopos.feature.details.DetailsViewModel
import ru.createsmart.artopos.feature.details.ui.components.DetailsContent
import ru.createsmart.artopos.feature.details.ui.preview.ArtworkDetailStateProvider
import ru.createsmart.artopos.core.ui.R as UiR

@Composable
fun DetailsScreenRoute(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val contentVersion by viewModel.contentVersion.collectAsStateWithLifecycle()

    DetailsScreen(
        state = state,
        contentVersion = contentVersion,
        effectFlow = viewModel.uiEffect,
        onRefresh = viewModel::onRefresh,
        onBackClick = onBackClick,
        isRefreshing = isRefreshing,
        onToggleTranslation = viewModel::toggleTranslation,
    )
}

@Composable
fun DetailsScreen(
    state: ArtworkDetailUiState,
    contentVersion: Int,
    effectFlow: Flow<UiText>? = null,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    isRefreshing: Boolean,
    onToggleTranslation: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val onShowSnackbar: (UiText) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    DiscoverScreenEffects(
        effectFlow = effectFlow,
        onShowSnackbar = onShowSnackbar,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (state) {
                is ArtworkDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }

                is ArtworkDetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(UiR.string.label_wrong),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is ArtworkDetailUiState.Success -> {
                    DetailsContent(
                        state.artwork,
                        contentVersion = contentVersion,
                        onShowMessage = { onShowSnackbar(it) },
                        onRefresh = onRefresh,
                        isRefreshing = isRefreshing,
                        onToggleTranslation = onToggleTranslation,
                    )
                }
            }

            BackButton(onBackClick)
        }
    }
}

@Composable
private fun DiscoverScreenEffects(
    effectFlow: Flow<UiText>?,
    onShowSnackbar: (UiText) -> Unit,
) {
    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message -> onShowSnackbar(message) }
    }
}

@Preview(showBackground = true, name = "Details States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DiscoverScreenPreview(
    @PreviewParameter(ArtworkDetailStateProvider::class) state: ArtworkDetailUiState,
) {
    ArtoposTheme {
        DetailsScreen(
            state = state,
            contentVersion = 0,
            effectFlow = null,
            onRefresh = { },
            onBackClick = { },
            isRefreshing = true,
            onToggleTranslation = { },
        )
    }
}
