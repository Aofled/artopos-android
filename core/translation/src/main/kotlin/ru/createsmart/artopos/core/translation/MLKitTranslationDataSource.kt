package ru.createsmart.artopos.core.translation

import android.content.res.Resources
import android.util.Log
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MLKitTranslationDataSource @Inject constructor() {

    private val mutex = Mutex()

    private var currentTranslator: Translator? = null
    private var activeLanguage: String = ""

    private fun getActualLanguage(targetLanguageCode: String): String {
        return targetLanguageCode.ifEmpty {
            Resources.getSystem().configuration.locales.get(0).language
        }
    }

    private suspend fun getOrCreateTranslator(targetLanguageCode: String): Translator? = mutex.withLock {
        val actualLangCode = getActualLanguage(targetLanguageCode)

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

    suspend fun translate(text: String?, targetLanguage: String): String? {
        // Try to get the translator only if text not empty.
        if (text.isNullOrBlank()) return text

        val translator = getOrCreateTranslator(targetLanguage)

        return if (translator != null) {
            try {
                // Critical: Ensure the language model (approx. 30MB) is downloaded before translating.
                // If offline and model missing -> throws Exception.
                // Language packs are downloaded via Wi-Fi and mobile internet (.requireWifi() <- only Wi-Fi)
                val conditions = DownloadConditions.Builder().build()
                translator.downloadModelIfNeeded(conditions).await()
                translator.translate(text).await()
            } catch (e: MlKitException) {
                Log.e("MLKitTranslator", "Failed to translate text. ML Kit Error Code: ${e.errorCode}", e)
                text
            }
        } else {
            // Stability: On failure (no internet, low storage), show original text instead of crashing.
            text
        }
    }

    suspend fun preloadModel(targetLanguage: String) {
        val translator = getOrCreateTranslator(targetLanguage) ?: return
        // Language packs are downloaded via Wi-Fi and mobile internet (.requireWifi() <- only Wi-Fi)
        try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
        } catch (e: MlKitException) {
            Log.e("MLKitTranslator", "Failed to preload ML model. ML Kit Error Code: ${e.errorCode}", e)
        }
    }

    suspend fun isModelDownloaded(targetLanguageCode: String): Boolean {
        val actualLangCode = getActualLanguage(targetLanguageCode)
        val targetLanguage = TranslateLanguage.fromLanguageTag(actualLangCode)

        if (actualLangCode == "en" || targetLanguage == null) {
            return true
        }

        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(targetLanguage).build()

        return try {
            modelManager.isModelDownloaded(model).await()
        } catch (e: MlKitException) {
            Log.e("MLKitTranslator", "Failed to check if model is downloaded", e)
            false
        }
    }

    suspend fun close() = mutex.withLock {
        currentTranslator?.close()
        currentTranslator = null
        activeLanguage = ""
    }
}
