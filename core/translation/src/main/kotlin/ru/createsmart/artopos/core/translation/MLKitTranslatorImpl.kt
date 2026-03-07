package ru.createsmart.artopos.core.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLKitTranslatorImpl @Inject constructor() : TextTranslator {

    private var translator: Translator? = null

    // Detects system language (e.g. "ru", "fr")
    private val targetLanguageCode: String = Locale.getDefault().language

    // Source content is always English (from Harvard API)
    private val sourceLanguageCode = TranslateLanguage.ENGLISH

    private fun getOrCreateTranslator(): Translator? {
        val existing = translator
        if (existing != null) return existing

        val targetLanguage = TranslateLanguage.fromLanguageTag(targetLanguageCode)

        // Logic: No translation needed if user's phone is already in English.
        val isLanguageSupported = targetLanguage != null && targetLanguage != sourceLanguageCode

        return if (isLanguageSupported && targetLanguage != null) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguage)
                .build()
            Translation.getClient(options).also { translator = it }
        } else {
            null // Return null to skip translation logic
        }
    }

    override suspend fun translate(text: String?): String? {
        val currentTranslator = getOrCreateTranslator()

        // Fallback: Return original text if translation is disabled or input is empty
        if (text.isNullOrBlank() || currentTranslator == null) {
            return text
        }

        return try {
            // Critical: Ensure the language model (approx. 30MB) is downloaded before translating.
            // If offline and model missing -> throws Exception.
            val conditions = DownloadConditions.Builder().build()
            currentTranslator.downloadModelIfNeeded(conditions).await()

            currentTranslator.translate(text).await()
        } catch (ignored: com.google.mlkit.common.MlKitException) {
            // Stability: On failure (no internet, low storage), show original text instead of crashing.
            text
        }
    }

    override suspend fun preloadModel() {
        val currentTranslator = getOrCreateTranslator() ?: return

        try {
            val conditions = DownloadConditions.Builder().build()
            currentTranslator.downloadModelIfNeeded(conditions).await()
        } catch (ignored: com.google.mlkit.common.MlKitException) {
            // Silent fail is OK for preloading
        }
    }

    override suspend fun isModelDownloaded(): Boolean {
        val targetLanguage = TranslateLanguage.fromLanguageTag(targetLanguageCode)
            ?: return true

        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(targetLanguage).build()

        return try {
            modelManager.isModelDownloaded(model).await()
        } catch (ignored: com.google.mlkit.common.MlKitException) {
            false
        }
    }
}
