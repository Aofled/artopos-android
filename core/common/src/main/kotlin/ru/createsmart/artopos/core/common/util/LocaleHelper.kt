package ru.createsmart.artopos.core.common.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Creates a localized context on demand without holding static references.
     * Prevents Activity memory leaks and guarantees fresh resources on configuration changes.
     */
    fun getLocalizedContext(baseContext: Context, languageTag: String): Context {
        if (languageTag.isBlank()) return baseContext

        val locale = Locale.forLanguageTag(languageTag)

        val configuration = Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }

        val localizedContext = baseContext.createConfigurationContext(configuration)

        // Wrap to preserve the original baseContext so findActivity() can still resolve the Window
        return object : ContextWrapper(localizedContext) {
            override fun getBaseContext(): Context = baseContext
        }
    }
}
