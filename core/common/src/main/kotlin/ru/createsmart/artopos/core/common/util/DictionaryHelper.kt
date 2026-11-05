package ru.createsmart.artopos.core.common.util

import android.content.Context
import androidx.annotation.StringRes
import ru.createsmart.artopos.core.common.util.dictionaries.CenturyDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.ClassificationsDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.CulturesDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.MediumDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.PeriodDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.TechniqueDictionary

object DictionaryHelper {

    /**
     * Searches existing dictionary maps with O(1) complexity per map.
     * Completely eliminates redundant merged HashMap allocation in Heap.
     */
    @StringRes
    fun getLocalizedResId(rawName: String): Int? {
        return ClassificationsDictionary[rawName]
            ?: CenturyDictionary[rawName]
            ?: CulturesDictionary[rawName]
            ?: MediumDictionary[rawName]
            ?: TechniqueDictionary[rawName]
            ?: PeriodDictionary[rawName]
    }

    fun getLocalizedName(context: Context, rawName: String): String {
        val resId = getLocalizedResId(rawName)
        return if (resId != null) context.getString(resId) else rawName
    }
}
