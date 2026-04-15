package ru.createsmart.artopos.core.data.repository

import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.translation.MLKitTranslationDataSource
import javax.inject.Inject

class TextTranslatorImpl @Inject constructor(
    private val dataSource: MLKitTranslationDataSource,
) : TextTranslator {

    override suspend fun translate(text: String?, targetLanguage: String): String? {
        return dataSource.translate(text, targetLanguage)
    }

    override suspend fun preloadModel(targetLanguage: String) {
        dataSource.preloadModel(targetLanguage)
    }

    override suspend fun isModelDownloaded(targetLanguageCode: String): Boolean {
        return dataSource.isModelDownloaded(targetLanguageCode)
    }
}
