package ru.createsmart.artopos.feature.details.translation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.ui.theme.util.FilterNameHelper
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
 * - description, dimensions, date
 */

class ArtworkTranslationFacade @Inject constructor(
    @ApplicationContext private val context: Context,
    private val translator: TextTranslator,
) {

    fun translateFast(artwork: Artwork): Artwork {
        return artwork.copy(
            // DICTIONARY
            classification = translateWithDictionary(artwork.classification),
            century = translateWithDictionary(artwork.century),
            culture = translateWithDictionary(artwork.culture),

            // HYBRID (DICTIONARY + ML KIT)
            technique = translateWithDictionary(artwork.technique),
            medium = translateWithDictionary(artwork.medium),
            period = translateWithDictionary(artwork.period),
        )
    }

    suspend fun translateDeep(
        originalArtwork: Artwork,
        fastArtwork: Artwork,
    ): Artwork {
        return withContext(Dispatchers.IO) {
            // ML_ONLY: ML Kit
            val descriptionDef = async { translator.translate(originalArtwork.description) }
            val dimensionsDef = async { translator.translate(originalArtwork.dimensions) }
            val dateDef = async { translator.translate(originalArtwork.date) }
            val styleDef = async { translator.translate(originalArtwork.style) }

            // HYBRID: Look it up in a dictionary, if not -> ML Kit
            val mediumDef = async {
                if (fastArtwork.medium != originalArtwork.medium) {
                    fastArtwork.medium
                } else {
                    translator.translate(originalArtwork.medium)
                }
            }

            val techniqueDef = async {
                if (fastArtwork.technique != originalArtwork.technique) {
                    fastArtwork.technique
                } else {
                    translator.translate(originalArtwork.technique)
                }
            }

            val periodDef = async {
                if (fastArtwork.period != originalArtwork.period) {
                    fastArtwork.period
                } else {
                    translator.translate(originalArtwork.period)
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
            )
        }
    }

    // Checks if string exists in local mapping
    private fun translateWithDictionary(text: String?): String? {
        if (text == null) return null
        val localized = FilterNameHelper.getLocalizedName(context, text)
        return localized
    }
}
