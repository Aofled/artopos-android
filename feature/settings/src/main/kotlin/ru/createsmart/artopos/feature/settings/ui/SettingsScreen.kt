package ru.createsmart.artopos.feature.settings.ui

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.feature.settings.R
import ru.createsmart.artopos.feature.settings.SettingsUiState
import ru.createsmart.artopos.feature.settings.SettingsViewModel
import ru.createsmart.artopos.feature.settings.model.SettingsDialogActions
import ru.createsmart.artopos.feature.settings.ui.components.ClearCacheConfirmationDialog
import ru.createsmart.artopos.feature.settings.ui.components.LanguageSelectionDialog
import ru.createsmart.artopos.feature.settings.ui.components.ThemeSelectionDialog
import ru.createsmart.artopos.feature.settings.ui.components.getLanguageDisplayName
import ru.createsmart.artopos.feature.settings.ui.components.getThemeDisplayName
import ru.createsmart.artopos.feature.settings.ui.preview.ArtworkSettingsStateProvider
import ru.createsmart.artopos.core.designsystem.R as UiR

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cacheSizeMb by viewModel.cacheSizeMb.collectAsStateWithLifecycle()
    val currentLanguageTag = if (uiState is SettingsUiState.Success) {
        (uiState as SettingsUiState.Success).settings.languageCode
    } else {
        ""
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) { // Recalculate the occupied cache
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.calculateCacheSize()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SettingsScreen(
        uiState = uiState,
        cacheSizeMb = cacheSizeMb,
        effectFlow = viewModel.uiEffect,
        onThemeChange = viewModel::updateTheme,
        onClearCache = viewModel::clearImageCache,
        onLanguageChange = viewModel::updateLanguage,
        currentLanguageTag = currentLanguageTag,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    cacheSizeMb: Long?,
    effectFlow: Flow<UiText>?,
    onThemeChange: (ThemeConfig) -> Unit,
    onClearCache: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguageTag: String,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 52.dp),
            )
        },
        topBar = { SettingsTopAppBar() },
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            cacheSizeMb = cacheSizeMb,
            currentLanguageTag = currentLanguageTag,
            onThemeChange = onThemeChange,
            onClearCache = onClearCache,
            onLanguageChange = onLanguageChange,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier,
    uiState: SettingsUiState,
    cacheSizeMb: Long?,
    currentLanguageTag: String,
    onThemeChange: (ThemeConfig) -> Unit,
    onClearCache: () -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    when (uiState) {
        is SettingsUiState.Loading -> Box(modifier.fillMaxSize())
        is SettingsUiState.Success -> {
            SettingsContent(
                modifier = modifier,
                settings = uiState.settings,
                cacheSizeMb = cacheSizeMb,
                onThemeClick = { showThemeDialog = true },
                onClearCacheClick = { showClearCacheDialog = true },
                onLanguageClick = { showLanguageDialog = true },
                currentLanguageTag = currentLanguageTag,
            )

            val dialogActions = SettingsDialogActions(
                onThemeChange = {
                    onThemeChange(it)
                    showThemeDialog = false
                },
                onClearCache = {
                    onClearCache()
                    showClearCacheDialog = false
                },
                onLanguageChange = { tag ->
                    onLanguageChange(tag)
                    showLanguageDialog = false
                },
                onDismissTheme = { showThemeDialog = false },
                onDismissClearCache = { showClearCacheDialog = false },
                onDismissLanguage = { showLanguageDialog = false },
            )

            SettingsDialogs(
                uiState = uiState,
                showTheme = showThemeDialog,
                showClearCache = showClearCacheDialog,
                showLanguage = showLanguageDialog,
                currentLanguageTag = currentLanguageTag,
                actions = dialogActions,
            )
        }
    }
}

@Composable
private fun SettingsDialogs(
    uiState: SettingsUiState.Success,
    showTheme: Boolean,
    showClearCache: Boolean,
    showLanguage: Boolean,
    currentLanguageTag: String,
    actions: SettingsDialogActions,
) {
    if (showTheme) {
        ThemeSelectionDialog(
            currentTheme = uiState.settings.themeConfig,
            onThemeSelected = actions.onThemeChange,
            onDismiss = actions.onDismissTheme,
        )
    }

    if (showClearCache) {
        ClearCacheConfirmationDialog(
            onConfirm = actions.onClearCache,
            onDismiss = actions.onDismissClearCache,
        )
    }

    if (showLanguage) {
        LanguageSelectionDialog(
            currentLanguageTag = currentLanguageTag,
            onLanguageSelected = actions.onLanguageChange,
            onDismiss = actions.onDismissLanguage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.title_settings),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    settings: UserSettings,
    cacheSizeMb: Long?,
    onThemeClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    currentLanguageTag: String,
    onLanguageClick: () -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SettingsSectionTitle(stringResource(R.string.title_appearance)) }

        item {
            SettingsItem(
                painter = painterResource(UiR.drawable.color_lens),
                title = stringResource(R.string.setting_theme),
                subtitle = getThemeDisplayName(settings.themeConfig),
                onClick = onThemeClick,
            )
        }

        item {
            SettingsItem(
                painter = painterResource(id = UiR.drawable.language),
                title = stringResource(R.string.setting_language),
                subtitle = getLanguageDisplayName(currentLanguageTag),
                onClick = onLanguageClick,
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionTitle(stringResource(R.string.title_storage))
        }

        item {
            val cacheSubtitle = if (cacheSizeMb == null) {
                stringResource(R.string.settings_cache_calculating)
            } else {
                stringResource(R.string.settings_cache_size_used, cacheSizeMb)
            }

            SettingsItem(
                painter = painterResource(UiR.drawable.delete_outline),
                title = stringResource(R.string.setting_cache),
                subtitle = cacheSubtitle,
                onClick = onClearCacheClick,
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsItem(
    painter: Painter,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Settings States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SettingsScreenPreview(
    @PreviewParameter(ArtworkSettingsStateProvider::class) state: SettingsUiState,
) {
    ArtoposTheme {
        SettingsScreen(
            uiState = state,
            cacheSizeMb = 142L,
            effectFlow = null,
            onThemeChange = { },
            onLanguageChange = { },
            onClearCache = { },
            currentLanguageTag = "en",
        )
    }
}
