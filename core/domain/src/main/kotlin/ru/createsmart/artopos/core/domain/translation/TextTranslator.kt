package ru.createsmart.artopos.core.domain.translation

public interface TextTranslator {
    public suspend fun preloadModel(targetLanguage: String) // Starts downloading the model
    public suspend fun translate(text: String?, targetLanguage: String): String?
    public suspend fun isModelDownloaded(targetLanguageCode: String): Boolean
    public suspend fun close()
}
