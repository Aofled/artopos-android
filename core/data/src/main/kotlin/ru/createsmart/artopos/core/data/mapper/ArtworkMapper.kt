package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkFeedWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ImageDimensionsDBO
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.ArtworkImage
import ru.createsmart.artopos.core.model.CreationDate
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import javax.inject.Inject

class ArtworkMapper @Inject constructor() {

    companion object {
        private val YEAR_REGEX = Regex("\\d{4}")
        private const val ROLE_ARTIST = "Artist"
    }

    // --- NETWORK (DTO) -> DATABASE (DBO) ---

    fun mapDtoToDbo(dto: ArtworkDTO): ArtworkDBO {
        val artistName = dto.artists
            ?.filter { it.role.equals(ROLE_ARTIST, ignoreCase = true) }
            ?.mapNotNull { it.name }
            ?.joinToString(", ")
            ?.takeIf { it.isNotBlank() }
            ?: ""

        val bestImage = dto.images?.firstOrNull()
        val finalUrl = bestImage?.url ?: dto.imageUrl ?: ""

        val dimensions = if (bestImage != null && bestImage.width > 0 && bestImage.height > 0) {
            ImageDimensionsDBO(width = bestImage.width, height = bestImage.height)
        } else {
            null
        }

        val parsedYear = dto.date?.let { YEAR_REGEX.find(it)?.value?.toIntOrNull() }

        val gallery = dto.images?.map { img ->
            StoredImage(url = img.url ?: "", width = img.width, height = img.height)
        }

        return ArtworkDBO(
            id = dto.id,
            title = dto.title.orEmpty(),
            artist = artistName,
            imageUrl = finalUrl,
            imageDimensions = dimensions,
            date = dto.date,
            yearInt = parsedYear,
            technique = dto.technique,
            description = dto.description,
            url = dto.webUrl,
            galleryImages = gallery,
        )
    }

    fun mapDtoToDetailsDbo(dto: ArtworkDTO): ArtworkDetailsDBO {
        return ArtworkDetailsDBO(
            id = dto.id,
            provenance = dto.provenance,
            creditLine = dto.creditLine,
            classification = dto.classification,
            century = dto.century,
            culture = dto.culture,
            medium = dto.medium,
            period = dto.period,
            style = dto.style,
            dimensions = dto.dimensions,
            copyright = dto.copyright,
            galleryLocation = dto.gallery?.name,
        )
    }

    // --- DATABASE (DBO) -> DOMAIN ---

    fun mapToDomain(dbo: ArtworkFeedWithFavoriteFlagDBO): Artwork {
        val domainDimensions = dbo.artwork.imageDimensions?.let {
            ImageDimensions(width = it.width, height = it.height)
        }

        return Artwork(
            id = dbo.artwork.id,
            title = dbo.artwork.title,
            artist = dbo.artwork.artist,
            imageUrl = dbo.artwork.imageUrl,
            imageDimensions = domainDimensions,
            creationDate = CreationDate.Unknown,
            isFavorite = dbo.isFavorite,
        )
    }

    fun mapDetailsToDomain(dbo: ArtworkDetailsWithFavoriteFlagDBO): ArtworkDetails {
        val yearInt = dbo.artworkWithDetails.artwork.yearInt
        val dateString = dbo.artworkWithDetails.artwork.date
        val creationDate = when {
            yearInt != null -> CreationDate.ExactYear(yearInt)
            !dateString.isNullOrBlank() -> CreationDate.TextOnly(dateString)
            else -> CreationDate.Unknown
        }

        val domainDimensions = dbo.artworkWithDetails.artwork.imageDimensions?.let {
            ImageDimensions(width = it.width, height = it.height)
        }

        val baseArtwork = Artwork(
            id = dbo.artworkWithDetails.artwork.id,
            title = dbo.artworkWithDetails.artwork.title,
            artist = dbo.artworkWithDetails.artwork.artist,
            imageUrl = dbo.artworkWithDetails.artwork.imageUrl,
            imageDimensions = domainDimensions,
            creationDate = creationDate,
            isFavorite = dbo.isFavorite,
        )

        // If the details are not in the database, we return what is available (+ gallery from the first request)
        val details = dbo.artworkWithDetails.details ?: return ArtworkDetails(
            baseArtwork = baseArtwork,
            technique = dbo.artworkWithDetails.artwork.technique,
            description = dbo.artworkWithDetails.artwork.description,
            url = dbo.artworkWithDetails.artwork.url,
            provenance = null, creditLine = null, classification = null, century = null,
            culture = null, medium = null, period = null, style = null,
            dimensions = null, copyright = null, galleryLocation = null,

            images = dbo.artworkWithDetails.artwork.galleryImages?.map { stored ->
                ArtworkImage(url = stored.url, width = stored.width, height = stored.height)
            } ?: emptyList(),
        )

        // If the parts are downloaded, return complete object
        return ArtworkDetails(
            baseArtwork = baseArtwork,
            technique = dbo.artworkWithDetails.artwork.technique,
            description = dbo.artworkWithDetails.artwork.description,
            url = dbo.artworkWithDetails.artwork.url,
            provenance = details.provenance,
            creditLine = details.creditLine,
            classification = details.classification,
            century = details.century,
            culture = details.culture,
            medium = details.medium,
            period = details.period,
            style = details.style,
            dimensions = details.dimensions,
            copyright = details.copyright,
            galleryLocation = details.galleryLocation,
            images = dbo.artworkWithDetails.artwork.galleryImages?.map { stored ->
                ArtworkImage(url = stored.url, width = stored.width, height = stored.height)
            } ?: emptyList(),
        )
    }
}
