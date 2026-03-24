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

    private var currentTranslator: Translator? = null
    private var activeLanguage: String = ""

    // Detects system language (e.g. "ru", "fr")
    private val targetLanguageCode: String = Locale.getDefault().language

    private fun getOrCreateTranslator(targetLanguageCode: String): Translator? {
        val actualLangCode = targetLanguageCode.ifEmpty { Locale.getDefault().language }

        return when {
            // 1. If the target language is English (source), translation no needed
            actualLangCode == "en" -> null

            // 2. If the language has not changed, return the "cached" translator
            currentTranslator != null && activeLanguage == actualLangCode -> currentTranslator

            // 3. If the language has changed, close the old one (free up memory) and create a new one
            else -> {
                currentTranslator?.close()

                TranslateLanguage.fromLanguageTag(actualLangCode)?.let { targetLanguage ->
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(targetLanguage)
                        .build()

                    activeLanguage = actualLangCode
                    currentTranslator = Translation.getClient(options)
                    currentTranslator // Back a new client
                }
            }
        }
    }

    override suspend fun translate(text: String?, targetLanguage: String): String? {
        // Try to get the translator only if text not empty.
        val translator = if (text.isNullOrBlank()) null else getOrCreateTranslator(targetLanguage)

        return translator?.let { client ->
            try {
                // Critical: Ensure the language model (approx. 30MB) is downloaded before translating.
                // If offline and model missing -> throws Exception.
                val conditions = DownloadConditions.Builder().requireWifi().build()
                client.downloadModelIfNeeded(conditions).await()
                client.translate(text!!).await()
            } catch (ignored: com.google.mlkit.common.MlKitException) {
                // Stability: On failure (no internet, low storage), show original text instead of crashing.
                text
            }
        } ?: text // If translator was null or text is empty, return the original
    }

    override suspend fun preloadModel(targetLanguage: String) {
        val translator = getOrCreateTranslator(targetLanguage) ?: return

        try {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()
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
