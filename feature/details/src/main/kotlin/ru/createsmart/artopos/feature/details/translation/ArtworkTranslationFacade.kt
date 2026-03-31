package ru.createsmart.artopos.feature.details.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.common.util.LocaleHelper
import ru.createsmart.artopos.core.designsystem.util.FilterNameHelper
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.model.Artwork
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

class ArtworkTranslationFacade @Inject constructor(
    @ApplicationContext private val baseContext: Context,
    private val translator: TextTranslator,
) {

    fun translateFast(artwork: Artwork, languageCode: String): Artwork {
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
        originalArtwork: Artwork,
        fastArtwork: Artwork,
        languageCode: String,
    ): Artwork {
        return withContext(Dispatchers.IO) {
            // ML_ONLY: ML Kit
            val descriptionDef = async { translator.translate(originalArtwork.description, languageCode) }
            val dimensionsDef = async { translator.translate(originalArtwork.dimensions, languageCode) }
            val dateDef = async { translator.translate(originalArtwork.date, languageCode) }
            val styleDef = async { translator.translate(originalArtwork.style, languageCode) }
            val galleryLocationDef = async { translator.translate(originalArtwork.galleryLocation, languageCode) }
            val creditLineDef = async { translator.translate(originalArtwork.creditLine, languageCode) }
            val provenanceDef = async { translator.translate(originalArtwork.provenance, languageCode) }

            // HYBRID: Look it up in a dictionary, if not -> ML Kit
            val mediumDef = async {
                if (fastArtwork.medium != originalArtwork.medium) {
                    fastArtwork.medium
                } else {
                    translator.translate(originalArtwork.medium, languageCode)
                }
            }

            val techniqueDef = async {
                if (fastArtwork.technique != originalArtwork.technique) {
                    fastArtwork.technique
                } else {
                    translator.translate(originalArtwork.technique, languageCode)
                }
            }

            val periodDef = async {
                if (fastArtwork.period != originalArtwork.period) {
                    fastArtwork.period
                } else {
                    translator.translate(originalArtwork.period, languageCode)
                }
            }

            fastArtwork.copy(
                description = descriptionDef.await(),
                medium = mediumDef.await(),
                technique = techniqueDef.await(),
                period = periodDef.await(),
                dimensions = dimensionsDef.await(),
                date = dateDef.await(),
                style = styleDef.await(),
                galleryLocation = galleryLocationDef.await(),
                creditLine = creditLineDef.await(),
                provenance = provenanceDef.await(),
            )
        }
    }

    // Checks if string exists in local mapping
    private fun translateWithDictionary(text: String?, context: Context): String? {
        if (text == null) return null
        val localized = FilterNameHelper.getLocalizedName(context, text)
        return localized
    }
}
