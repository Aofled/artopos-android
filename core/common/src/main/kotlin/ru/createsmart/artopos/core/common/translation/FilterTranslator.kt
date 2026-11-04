package ru.createsmart.artopos.core.common.translation

interface FilterTranslator {
    fun translate(rawName: String, languageCode: String): String
    fun translate(rawName: String): String = translate(rawName, "")
}
