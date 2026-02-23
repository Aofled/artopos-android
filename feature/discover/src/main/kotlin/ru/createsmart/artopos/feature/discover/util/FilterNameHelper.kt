package ru.createsmart.artopos.feature.discover.util

import android.annotation.SuppressLint
import android.content.Context

object FilterNameHelper {

    /**
     * Converts a server name to a localized string.
     *
     * Logic:
     * 1. Input string: "19th century"
     * 2. Generated key: "filter_19th_century" (lowercase, spaces -> underscores, remove special characters)
     * 3. Search in R.string.filter_19th_century
     */
    @SuppressLint("DiscouragedApi")
    fun getLocalizedName(context: Context, rawName: String): String {
        if (rawName.isBlank()) return rawName

        val safeKey = rawName.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')

        val resourceName = "filter_$safeKey"

        val resId = context.resources.getIdentifier(
            resourceName,
            "string",
            context.packageName,
        )

        return if (resId != 0) {
            context.getString(resId)
        } else {
            rawName
        }
    }
}
