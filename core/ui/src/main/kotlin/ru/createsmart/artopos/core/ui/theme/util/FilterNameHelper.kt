package ru.createsmart.artopos.core.ui.theme.util

import android.annotation.SuppressLint
import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

// https://api.harvardartmuseums.org/classifications?apikey=API_KEY&sort=objectcount&sortorder=desc&size=500
object FilterNameHelper {
    private var cachedMap: Map<String, Int>? = null

    private val FILTER_FILES = listOf(
        "dictionaries/classifications.json",
        "dictionaries/century.json",
        "dictionaries/cultures.json",
        "dictionaries/medium.json",
        "dictionaries/technique.json",
        "dictionaries/period.json",
    )

    /**
     * Converts a server name to a localized string.
     */
    fun getLocalizedName(context: Context, rawName: String): String {
        if (cachedMap == null) {
            cachedMap = loadAllFilters(context)
        }

        val resId = cachedMap?.get(rawName)

        return if (resId != null && resId != 0) {
            context.getString(resId)
        } else {
            rawName
        }
    }

    private fun loadAllFilters(context: Context): Map<String, Int> {
        val mergedMap = mutableMapOf<String, Int>()

        FILTER_FILES.forEach { filePath ->
            val fileMap = loadMapFromAssets(context, filePath)
            mergedMap.putAll(fileMap)
        }

        return mergedMap
    }

    @SuppressLint("DiscouragedApi")
    private fun loadMapFromAssets(context: Context, filePath: String): Map<String, Int> {
        val tempMap = mutableMapOf<String, Int>()
        try {
            val jsonString = context.assets.open(filePath)
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val resourceName = jsonObject.getString(key)
                val resId = context.resources.getIdentifier(
                    resourceName,
                    "string",
                    context.packageName,
                )

                if (resId != 0) {
                    tempMap[key] = resId
                }
            }
        } catch (ignored: IOException) {
            // Log warning or leave empty for safety
        } catch (ignored: JSONException) {
            // Log warning or leave empty for safety
        }
        return tempMap
    }
}
