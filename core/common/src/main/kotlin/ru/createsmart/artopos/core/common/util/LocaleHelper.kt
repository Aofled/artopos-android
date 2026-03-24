package ru.createsmart.artopos.core.common.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun getLocalizedContext(baseContext: Context, languageTag: String): Context {
        if (languageTag.isEmpty()) return baseContext

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(locale)

        val localizedContext = baseContext.createConfigurationContext(configuration)

        /**
         * IMPORTANT: We wrap the localizedContext and return the original baseContext
         * as the base. This is necessary for Hilt to find the Activity
         * and create the ViewModelFactory without errors.
         */
        return object : ContextWrapper(localizedContext) {
            override fun getBaseContext(): Context = baseContext
        }
    }
}
