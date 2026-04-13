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
import ru.createsmart.artopos.core.designsystem.R as DSR

/**
 * IMPORTANT ARCHITECTURAL RULE FOR DIALOGS (Localization Note):
 *
 * In Jetpack Compose, the [AlertDialog] and [ModalBottomSheet] components create
 * a separate system window (Window/Popup) on top of the current UI tree.
 *
 * Because of this, they "break away" from our custom [CompositionLocalProvider],
 * where we change the app language on the fly (without restarting the Activity).
 *
 * If you call `stringResource()` INSIDE the `title`, `text`, or `confirmButton` lambdas,
 * the dialog uses the Android system language, ignoring the selected app language.
 *
 * THE CORRECT APPROACH (State Hoisting for Strings):
 * Always evaluate string resources (via `stringResource`) BEFORE calling `AlertDialog`,
 * store them in local variables `val text = ...` and pass them into the dialog
 * pre-built [String] objects.
 */

@Composable
fun ClearCacheConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val titleText = stringResource(R.string.settings_label_cache)
    val messageText = stringResource(R.string.settings_cache_dialog_message)
    val confirmText = stringResource(R.string.settings_action_clear_cache)
    val cancelText = stringResource(DSR.string.core_btn_cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(DSR.drawable.ic_delete), null) },
        title = { Text(titleText) },
        text = { Text(messageText) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
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
    val titleText = stringResource(R.string.settings_theme_dialog_title)
    val cancelText = stringResource(DSR.string.core_btn_cancel)

    val systemText = stringResource(R.string.settings_theme_option_system)
    val lightText = stringResource(R.string.settings_theme_option_light)
    val darkText = stringResource(R.string.settings_theme_option_dark)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(Modifier.selectableGroup()) {
                ThemeConfig.entries.forEach { themeOption ->
                    val displayName = when (themeOption) {
                        ThemeConfig.FOLLOW_SYSTEM -> systemText
                        ThemeConfig.LIGHT -> lightText
                        ThemeConfig.DARK -> darkText
                    }

                    ThemeOptionRow(
                        displayName = displayName,
                        isSelected = themeOption == currentTheme,
                        onClick = { onThemeSelected(themeOption) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        },
    )
}

@Composable
private fun ThemeOptionRow(
    displayName: String,
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
            text = displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
fun getThemeDisplayName(themeConfig: ThemeConfig): String {
    return when (themeConfig) {
        ThemeConfig.FOLLOW_SYSTEM -> stringResource(R.string.settings_theme_option_system)
        ThemeConfig.LIGHT -> stringResource(R.string.settings_theme_option_light)
        ThemeConfig.DARK -> stringResource(R.string.settings_theme_option_dark)
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val titleText = stringResource(R.string.settings_language_dialog_title)
    val cancelText = stringResource(DSR.string.core_btn_cancel)
    val unknownText = stringResource(R.string.settings_language_option_unknown)

    val languageNames = LanguageConfig.supportedLanguages.map { language ->
        val name = when {
            language.nativeName != null -> language.nativeName
            language.nameResId != null -> stringResource(language.nameResId!!)
            else -> LocaleListCompat.forLanguageTags(language.tag).get(0)?.displayName ?: unknownText
        }
        language.tag to name
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(Modifier.selectableGroup()) {
                languageNames.forEach { (tag, displayName) ->
                    LanguageOptionRow(
                        displayName = displayName,
                        isSelected = (tag == currentLanguageTag),
                        onClick = { onLanguageSelected(tag) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        },
    )
}

@Composable
private fun LanguageOptionRow(
    displayName: String,
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
            text = displayName,
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
            ?: stringResource(R.string.settings_language_option_unknown)
    }
}
