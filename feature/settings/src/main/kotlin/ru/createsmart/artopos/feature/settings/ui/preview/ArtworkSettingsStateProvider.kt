package ru.createsmart.artopos.feature.settings.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.feature.settings.SettingsUiState

class ArtworkSettingsStateProvider : PreviewParameterProvider<SettingsUiState> {
    override val values: Sequence<SettingsUiState> = sequenceOf(
        SettingsUiState.Success(
            settings = UserSettings(
                themeConfig = ThemeConfig.FOLLOW_SYSTEM,
                languageCode = "",
            ),
        ),
        SettingsUiState.Success(
            settings = UserSettings(
                themeConfig = ThemeConfig.DARK,
                languageCode = "ru",
            ),
        ),
        SettingsUiState.Loading,
    )
}
