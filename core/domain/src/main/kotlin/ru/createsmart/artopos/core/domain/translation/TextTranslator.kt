package ru.createsmart.artopos.core.domain.translation

interface TextTranslator {
    suspend fun preloadModel() // Starts downloading the model
    suspend fun isModelDownloaded(): Boolean
    suspend fun translate(text: String?): String?
}
