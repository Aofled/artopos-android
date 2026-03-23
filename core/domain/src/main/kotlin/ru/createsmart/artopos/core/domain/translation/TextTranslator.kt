package ru.createsmart.artopos.core.domain.translation

interface TextTranslator {
    suspend fun preloadModel(targetLanguage: String) // Starts downloading the model
    suspend fun isModelDownloaded(): Boolean
    suspend fun translate(text: String?, targetLanguage: String): String?
}
