package ru.createsmart.artopos.core.common.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.createsmart.artopos.core.common.util.DictionaryHelper
import ru.createsmart.artopos.core.common.util.LocaleHelper
import javax.inject.Inject

class FilterTranslatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FilterTranslator {

    override fun translate(rawName: String, languageCode: String): String {
        val locContext = LocaleHelper.getLocalizedContext(context, languageCode)
        return DictionaryHelper.getLocalizedName(locContext, rawName)
    }
}
