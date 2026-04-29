package ru.createsmart.artopos.feature.details.translation

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.common.util.DictionaryHelper
import ru.createsmart.artopos.core.common.util.LocaleHelper
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.CreationDate
import javax.inject.Inject

/**
 * Strategy:
 * No translation:
 * - title, artist, galleryLocation, creditLine, provenance, copyright
 * Translate from a local dictionary:
 * - classification, century, culture
 * Translate from a local dictionary, if that doesn't work, use ML KIT:
 * - technique, medium, period
 * Translate only ML KIT:
 * - description, dimensions, date, galleryLocation, creditLine, provenance
 */

private const val LOW_THREADS = 3
private const val LARGE_THREADS = 8

class ArtworkTranslationFacade @Inject constructor(
    @ApplicationContext private val baseContext: Context,
    private val translator: TextTranslator,
) {
    /**
     * Dynamic parallelism limit for ML Kit.
     * On budget devices (low RAM), we strictly limit the number of
     * simultaneously running heavy C++ threads to 3 to avoid OOMs.
     * On standard/flagship devices, we set the limit to 8,
     * to utilize all high-performance processor cores and translate text instantly.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val mlKitDispatcher: CoroutineDispatcher = run {
        val activityManager = baseContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isLowRam = activityManager.isLowRamDevice

        val threadLimit = if (isLowRam) LOW_THREADS else LARGE_THREADS
        Dispatchers.IO.limitedParallelism(threadLimit)
    }

    fun translateFast(artwork: ArtworkDetails, languageCode: String): ArtworkDetails {
        val localizedContext = LocaleHelper.getLocalizedContext(baseContext, languageCode)

        return artwork.copy(
            // DICTIONARY
            classification = translateWithDictionary(artwork.classification, localizedContext),
            century = translateWithDictionary(artwork.century, localizedContext),
            culture = translateWithDictionary(artwork.culture, localizedContext),

            // HYBRID (DICTIONARY + ML KIT)
            technique = translateWithDictionary(artwork.technique, localizedContext),
            medium = translateWithDictionary(artwork.medium, localizedContext),
            period = translateWithDictionary(artwork.period, localizedContext),
        )
    }

    suspend fun translateDeep(
        originalArtwork: ArtworkDetails,
        fastArtwork: ArtworkDetails,
        languageCode: String,
    ): ArtworkDetails {
        return withContext(mlKitDispatcher) {
            // 1. Availability of a language model
            translator.preloadModel(languageCode)

            // 2. Starting the translation
            val descDef = async { translator.translate(originalArtwork.description, languageCode) }
            val dimDef = async { translator.translate(originalArtwork.dimensions, languageCode) }
            val styleDef = async { translator.translate(originalArtwork.style, languageCode) }
            val locDef = async { translator.translate(originalArtwork.galleryLocation, languageCode) }
            val creditDef = async { translator.translate(originalArtwork.creditLine, languageCode) }
            val provDef = async { translator.translate(originalArtwork.provenance, languageCode) }

            // HYBRID & CUSTOM: Delegates
            val dateDef = async { translateDate(originalArtwork.baseArtwork.creationDate, languageCode) }
            val mediumDef = async { translateHybrid(originalArtwork.medium, fastArtwork.medium, languageCode) }
            val techDef = async { translateHybrid(originalArtwork.technique, fastArtwork.technique, languageCode) }
            val periodDef = async { translateHybrid(originalArtwork.period, fastArtwork.period, languageCode) }

            // 3. Assemble the result
            val updatedBaseArtwork = fastArtwork.baseArtwork.copy(
                creationDate = dateDef.await(),
            )

            fastArtwork.copy(
                baseArtwork = updatedBaseArtwork,
                description = descDef.await(),
                medium = mediumDef.await(),
                technique = techDef.await(),
                period = periodDef.await(),
                dimensions = dimDef.await(),
                style = styleDef.await(),
                galleryLocation = locDef.await(),
                creditLine = creditDef.await(),
                provenance = provDef.await(),
            )
        }
    }

    private suspend fun translateDate(
        currentDate: CreationDate,
        languageCode: String,
    ): CreationDate {
        return when (currentDate) {
            is CreationDate.TextOnly -> {
                val translatedText = translator.translate(currentDate.text, languageCode)
                if (translatedText != null) {
                    CreationDate.TextOnly(translatedText)
                } else {
                    currentDate // If the translation fails, we leave the original
                }
            }
            else -> currentDate // Numbers (1890) or unknown dates are not translated
        }
    }

    private suspend fun translateHybrid(
        originalText: String?,
        fastText: String?,
        languageCode: String,
    ): String? {
        return if (fastText != originalText) {
            fastText // Dictionary already translated it
        } else {
            translator.translate(originalText, languageCode) // ML Kit fallback
        }
    }

    // Checks if string exists in local mapping
    private fun translateWithDictionary(text: String?, context: Context): String? {
        if (text == null) return null
        val localized = DictionaryHelper.getLocalizedName(context, text)
        return localized
    }

    suspend fun close() {
        translator.close()
    }
}
