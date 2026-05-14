package ru.createsmart.artopos.feature.settings.ui

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.feature.settings.R
import ru.createsmart.artopos.feature.settings.SettingsUiState
import ru.createsmart.artopos.feature.settings.SettingsViewModel
import ru.createsmart.artopos.feature.settings.model.SettingsIntent
import ru.createsmart.artopos.feature.settings.ui.components.ClearCacheConfirmationDialog
import ru.createsmart.artopos.feature.settings.ui.components.LanguageSelectionDialog
import ru.createsmart.artopos.feature.settings.ui.components.ThemeSelectionDialog
import ru.createsmart.artopos.feature.settings.ui.components.getLanguageDisplayName
import ru.createsmart.artopos.feature.settings.ui.components.getThemeDisplayName
import ru.createsmart.artopos.feature.settings.ui.preview.ArtworkSettingsStateProvider
import ru.createsmart.artopos.core.designsystem.R as DSR

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val successState = uiState as? SettingsUiState.Success
    val currentLanguageTag = successState?.settings?.languageCode.orEmpty()
    val cacheSizeMb = successState?.cacheSizeMb

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) { // Recalculate the occupied cache
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(SettingsIntent.RecalculateCache)
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
        currentLanguageTag = currentLanguageTag,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    cacheSizeMb: Long?,
    effectFlow: Flow<UiText>?,
    currentLanguageTag: String,
    onIntent: (SettingsIntent) -> Unit,
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
                    .padding(bottom = ArtoposDimens.BottomBarHeight + 4.dp),
            )
        },
        topBar = { SettingsTopAppBar() },
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            cacheSizeMb = cacheSizeMb,
            currentLanguageTag = currentLanguageTag,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier,
    uiState: SettingsUiState,
    cacheSizeMb: Long?,
    currentLanguageTag: String,
    onIntent: (SettingsIntent) -> Unit,
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
                currentLanguageTag = currentLanguageTag,
                onThemeClick = { showThemeDialog = true },
                onClearCacheClick = { showClearCacheDialog = true },
                onLanguageClick = { showLanguageDialog = true },
            )

            SettingsDialogs(
                uiState = uiState,
                showTheme = showThemeDialog,
                showClearCache = showClearCacheDialog,
                showLanguage = showLanguageDialog,
                currentLanguageTag = currentLanguageTag,
                onIntent = onIntent,
                onDismissTheme = { showThemeDialog = false },
                onDismissClearCache = { showClearCacheDialog = false },
                onDismissLanguage = { showLanguageDialog = false },
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
    onIntent: (SettingsIntent) -> Unit,
    onDismissTheme: () -> Unit,
    onDismissClearCache: () -> Unit,
    onDismissLanguage: () -> Unit,
) {
    if (showTheme) {
        ThemeSelectionDialog(
            currentTheme = uiState.settings.themeConfig,
            onThemeSelected = {
                onIntent(SettingsIntent.UpdateTheme(it))
                onDismissTheme()
            },
            onDismiss = onDismissTheme,
        )
    }

    if (showClearCache) {
        ClearCacheConfirmationDialog(
            onConfirm = {
                onIntent(SettingsIntent.ClearCache)
                onDismissClearCache()
            },
            onDismiss = onDismissClearCache,
        )
    }

    if (showLanguage) {
        LanguageSelectionDialog(
            currentLanguageTag = currentLanguageTag,
            onLanguageSelected = {
                onIntent(SettingsIntent.UpdateLanguage(it))
                onDismissLanguage()
            },
            onDismiss = onDismissLanguage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(DSR.string.core_title_settings),
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
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item { SettingsSectionTitle(stringResource(R.string.settings_section_appearance)) }

            item {
                SettingsItem(
                    painter = painterResource(DSR.drawable.ic_color_lens),
                    title = stringResource(R.string.settings_label_theme),
                    subtitle = getThemeDisplayName(settings.themeConfig),
                    onClick = onThemeClick,
                )
            }

            item {
                SettingsItem(
                    painter = painterResource(id = DSR.drawable.ic_language),
                    title = stringResource(R.string.settings_label_language),
                    subtitle = getLanguageDisplayName(currentLanguageTag),
                    onClick = onLanguageClick,
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_storage))
            }

            item {
                val cacheSubtitle = if (cacheSizeMb == null) {
                    stringResource(R.string.settings_cache_status_calculating)
                } else {
                    stringResource(R.string.settings_cache_status_size_used, cacheSizeMb)
                }

                SettingsItem(
                    painter = painterResource(DSR.drawable.ic_delete),
                    title = stringResource(R.string.settings_label_cache),
                    subtitle = cacheSubtitle,
                    onClick = onClearCacheClick,
                )
            }
        }

        val versionName = getAppVersionName()
        if (versionName.isNotEmpty()) {
            val navBarsHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            val bottomSafePadding = 16.dp + ArtoposDimens.BottomBarHeight + navBarsHeight

            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = bottomSafePadding, end = 24.dp),
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier) {
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
private fun SettingsItem(
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

@Composable
private fun getAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("SettingsScreen", "Failed to get app version", e)
            ""
        }
    }
}

@Preview(showBackground = true, name = "Settings States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(ArtworkSettingsStateProvider::class) state: SettingsUiState,
) {
    ArtoposTheme {
        SettingsScreen(
            uiState = state,
            cacheSizeMb = 142L,
            effectFlow = null,
            currentLanguageTag = "en",
            onIntent = { },
        )
    }
}
