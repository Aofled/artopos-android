package ru.createsmart.artopos.core.common.util

import android.content.Context
import androidx.annotation.StringRes
import ru.createsmart.artopos.core.common.util.dictionaries.CenturyDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.ClassificationsDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.CulturesDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.MediumDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.PeriodDictionary
import ru.createsmart.artopos.core.common.util.dictionaries.TechniqueDictionary

private const val HASH_MAP_LOAD_FACTOR = 0.75f

object DictionaryHelper {

    private val totalValues: Int
        get() = ClassificationsDictionary.size +
            CenturyDictionary.size +
            CulturesDictionary.size +
            MediumDictionary.size +
            TechniqueDictionary.size +
            PeriodDictionary.size

    // Key - English string from Harvard's backend
    // Value - reference to a string resource
    private val globalDictionary: Map<String, Int> by lazy {
        // Calculate the correct capacity taking into account the load factor
        val initialCapacity = (totalValues / HASH_MAP_LOAD_FACTOR + 1.0f).toInt()
        val merged = HashMap<String, Int>(initialCapacity) // Pre-allocate capacity for speed
        merged.putAll(ClassificationsDictionary)
        merged.putAll(CenturyDictionary)
        merged.putAll(CulturesDictionary)
        merged.putAll(MediumDictionary)
        merged.putAll(TechniqueDictionary)
        merged.putAll(PeriodDictionary)
        merged
    }

    /**
     * Returns the string resource ID if the translation is found in the dictionary.
     * Otherwise, returns null (meaning use the original text from the API).
     */
    @StringRes
    fun getLocalizedResId(rawName: String): Int? {
        return globalDictionary[rawName]
    }

    fun getLocalizedName(context: Context, rawName: String): String {
        val resId = getLocalizedResId(rawName)
        return if (resId != null) context.getString(resId) else rawName
    }
}
