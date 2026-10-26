package ru.createsmart.artopos.core.translation

import android.util.Log
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.translation.di.TranslationDispatcher
import java.util.Locale
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Single instance per app to save RAM
class MLKitTranslationDataSource @Inject constructor(
    @TranslationDispatcher private val dispatcher: CoroutineDispatcher,
) {

    private val mutex = Mutex()
    private var currentTranslator: Translator? = null
    private var activeLanguage: String = ""

    // Get default locale safely
    private fun getActualLanguage(targetLanguageCode: String): String {
        return targetLanguageCode.ifEmpty {
            Locale.getDefault().language
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
                // Close old translator before creating a new one
                try {
                    currentTranslator?.close()
                } catch (e: MlKitException) {
                    Log.w("MLKitTranslator", "Error closing old translator (MlKitException)", e)
                } catch (e: IllegalStateException) {
                    Log.w("MLKitTranslator", "Error closing old translator (IllegalStateException)", e)
                }

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

    suspend fun translate(text: String?, targetLanguage: String): String? = withContext(dispatcher) {
        if (text.isNullOrBlank()) return@withContext text
        val translator = getOrCreateTranslator(targetLanguage) ?: return@withContext text

        try {
            // Run on limited thread pool
            translator.translate(text).await()
        } catch (c: CancellationException) {
            throw c
        } catch (e: MlKitException) {
            Log.e("MLKitTranslator", "Failed to translate text (MlKitException).", e)
            text
        } catch (e: ExecutionException) {
            Log.e("MLKitTranslator", "Failed to translate text (ExecutionException).", e)
            text
        } catch (e: IllegalStateException) {
            Log.e("MLKitTranslator", "Failed to translate text (IllegalStateException).", e)
            text
        }
    }

    suspend fun preloadModel(targetLanguage: String) = withContext(dispatcher) {
        val translator = getOrCreateTranslator(targetLanguage) ?: return@withContext
        try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
        } catch (c: CancellationException) {
            throw c
        } catch (e: MlKitException) {
            Log.e("MLKitTranslator", "Failed to preload ML model (MlKitException).", e)
        } catch (e: ExecutionException) {
            Log.e("MLKitTranslator", "Failed to preload ML model (ExecutionException).", e)
        } catch (e: IllegalStateException) {
            Log.e("MLKitTranslator", "Failed to preload ML model (IllegalStateException).", e)
        }
    }

    suspend fun isModelDownloaded(targetLanguageCode: String): Boolean = withContext(dispatcher) {
        val actualLangCode = getActualLanguage(targetLanguageCode)
        val targetLanguage = TranslateLanguage.fromLanguageTag(actualLangCode)

        if (actualLangCode == "en" || targetLanguage == null) {
            return@withContext true
        }

        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(targetLanguage).build()

        return@withContext try {
            modelManager.isModelDownloaded(model).await()
        } catch (c: CancellationException) {
            throw c
        } catch (e: MlKitException) {
            Log.e("MLKitTranslator", "Failed to check if model is downloaded", e)
            false
        } catch (e: ExecutionException) {
            Log.e("MLKitTranslator", "Failed to check if model is downloaded", e)
            false
        } catch (e: IllegalStateException) {
            Log.e("MLKitTranslator", "Failed to check if model is downloaded", e)
            false
        }
    }

    suspend fun close() = mutex.withLock {
        try {
            currentTranslator?.close()
        } catch (e: MlKitException) {
            Log.w("MLKitTranslator", "Error during close (MlKitException)", e)
        } catch (e: IllegalStateException) {
            Log.w("MLKitTranslator", "Error during close (IllegalStateException)", e)
        } finally {
            currentTranslator = null
            activeLanguage = ""
        }
    }
}
