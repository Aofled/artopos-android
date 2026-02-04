package ru.createsmart.artopos.feature.discover.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.DiscoverUiState
import ru.createsmart.artopos.feature.discover.DiscoverViewModel
import ru.createsmart.artopos.feature.discover.ui.components.ArtworksView
import ru.createsmart.artopos.feature.discover.ui.components.ErrorView
import ru.createsmart.artopos.feature.discover.ui.components.LoadingView
import ru.createsmart.artopos.feature.discover.ui.preview.DiscoverStateProvider

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onArtworkClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DiscoverScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onArtworkClick = onArtworkClick,
    )
}

@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    onRefresh: () -> Unit,
    onArtworkClick: (Int) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            when (state) {
                is DiscoverUiState.Loading -> {
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        LoadingView()
                    }
                }
                is DiscoverUiState.Error -> {
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        ErrorView(onRetry = onRefresh)
                    }
                }
                is DiscoverUiState.Success -> {
                    ArtworksView(
                        artworks = state.artworks,
                        contentVersion = state.contentVersion,
                        isRefreshing = state.isRefreshing,
                        onRefresh = onRefresh,
                        onArtworkClick = onArtworkClick,
                        contentPadding = innerPadding,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Discover States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DiscoverScreenPreview(
    @PreviewParameter(DiscoverStateProvider::class) state: DiscoverUiState,
) {
    ArtoposTheme {
        DiscoverScreen(
            state = state,
            onRefresh = { },
            onArtworkClick = { id -> println("Clicked id: $id") },
        )
    }
}
