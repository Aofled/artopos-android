package ru.createsmart.artopos.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.feature.settings.R
import ru.createsmart.artopos.feature.settings.model.LanguageConfig
import ru.createsmart.artopos.feature.settings.model.LanguageItem
import ru.createsmart.artopos.core.designsystem.R as UiR

@Composable
fun ClearCacheConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(UiR.drawable.delete_outline), null) },
        title = { Text(stringResource(R.string.setting_cache)) },
        text = { Text(stringResource(R.string.clear_cache_description)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_clear), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeConfig,
    onThemeSelected: (ThemeConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_theme)) },
        text = {
            Column(Modifier.selectableGroup()) {
                ThemeConfig.entries.forEach { themeOption ->
                    ThemeOptionRow(
                        themeOption = themeOption,
                        isSelected = themeOption == currentTheme,
                        onClick = { onThemeSelected(themeOption) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun ThemeOptionRow(
    themeOption: ThemeConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(
            text = getThemeDisplayName(themeOption),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
fun getThemeDisplayName(themeConfig: ThemeConfig): String {
    return when (themeConfig) {
        ThemeConfig.FOLLOW_SYSTEM -> stringResource(R.string.theme_system)
        ThemeConfig.LIGHT -> stringResource(R.string.theme_light)
        ThemeConfig.DARK -> stringResource(R.string.theme_dark)
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_dialog_title)) },
        text = {
            Column(Modifier.selectableGroup()) {
                LanguageConfig.supportedLanguages.forEach { language ->
                    LanguageOptionRow(
                        language = language,
                        isSelected = (language.tag == currentLanguageTag),
                        onClick = { onLanguageSelected(language.tag) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun LanguageOptionRow(
    language: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(
            text = language.nativeName ?: stringResource(language.nameResId!!),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
fun getLanguageDisplayName(languageTag: String): String {
    val language = LanguageConfig.supportedLanguages.find { it.tag == languageTag }
    return when {
        language?.nativeName != null -> language.nativeName
        language?.nameResId != null -> stringResource(language.nameResId)
        else -> LocaleListCompat.forLanguageTags(languageTag).get(0)?.displayName
            ?: stringResource(R.string.language_unknown)
    }
}
