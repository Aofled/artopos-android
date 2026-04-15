package ru.createsmart.artopos.core.domain.translation

interface TextTranslator {
    suspend fun preloadModel(targetLanguage: String) // Starts downloading the model
    suspend fun translate(text: String?, targetLanguage: String): String?
    suspend fun isModelDownloaded(targetLanguageCode: String): Boolean
}
