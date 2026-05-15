package ru.createsmart.artopos.feature.details.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.components.BackButton
import ru.createsmart.artopos.feature.details.ArtworkDetailUiState
import ru.createsmart.artopos.feature.details.DetailsViewModel
import ru.createsmart.artopos.feature.details.model.DetailsIntent
import ru.createsmart.artopos.feature.details.ui.components.DetailsContent
import ru.createsmart.artopos.feature.details.ui.preview.ArtworkDetailStateProvider
import ru.createsmart.artopos.core.designsystem.R as DSR

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
        isRefreshing = isRefreshing,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
    )
}

@Composable
fun DetailsScreen(
    state: ArtworkDetailUiState,
    contentVersion: Int,
    effectFlow: Flow<UiText>?,
    isRefreshing: Boolean,
    onIntent: (DetailsIntent) -> Unit,
    onBackClick: () -> Unit,
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
        contentWindowInsets = WindowInsets.statusBars,
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
                            text = stringResource(DSR.string.core_error_generic),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is ArtworkDetailUiState.Success -> {
                    DetailsContent(
                        artwork = state.artwork,
                        contentVersion = contentVersion,
                        isRefreshing = isRefreshing,
                        onIntent = onIntent,
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
            onBackClick = { },
            isRefreshing = true,
            onIntent = { },
        )
    }
}
