package ru.createsmart.artopos.core.common.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object LocaleHelper {

    // Cache contexts to avoid heavy recreation.
    private val contextCache = ConcurrentHashMap<String, Context>()

    fun getLocalizedContext(baseContext: Context, languageTag: String): Context {
        // Return base context if language is not set.
        if (languageTag.isEmpty()) return baseContext

        return contextCache.getOrPut(languageTag) {
            val locale = Locale.forLanguageTag(languageTag)

            // Do not call Locale.setDefault to avoid changing global app locale.
            val configuration = Configuration(baseContext.resources.configuration).apply {
                setLocale(locale)
            }

            val localizedContext = baseContext.createConfigurationContext(configuration)

            // Wrap context to keep base context for Hilt and Activity.
            object : ContextWrapper(localizedContext) {
                override fun getBaseContext(): Context = baseContext
            }
        }
    }
}
