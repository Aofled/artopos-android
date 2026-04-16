package ru.createsmart.artopos.core.uicomponents.locale

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import ru.createsmart.artopos.core.common.util.LocaleHelper

@Composable
fun LocaleProvider(
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val baseContext: Context = LocalContext.current

    val localizedContext: Context = remember(languageCode) {
        LocaleHelper.getLocalizedContext(baseContext, languageCode)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
    ) {
        content()
    }
}
