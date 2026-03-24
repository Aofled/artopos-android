package ru.createsmart.artopos.feature.settings.model

import ru.createsmart.artopos.core.model.settings.ThemeConfig

data class SettingsDialogActions(
    val onThemeChange: (ThemeConfig) -> Unit,
    val onClearCache: () -> Unit,
    val onLanguageChange: (String) -> Unit,
    val onDismissTheme: () -> Unit,
    val onDismissClearCache: () -> Unit,
    val onDismissLanguage: () -> Unit,
)
