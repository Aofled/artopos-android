@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtworkDTO(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("dated") val date: String? = null, // "1889", "17th century"
    @SerialName("technique") val technique: String? = null, // "Oil on canvas"
    @SerialName("primaryimageurl") val imageUrl: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val webUrl: String? = null, // Link to museum site
    @SerialName("people") val artists: List<PersonDTO> = emptyList(),
    @SerialName("images") val images: List<ImageDTO>? = emptyList(),
    @SerialName("places") val places: List<PlaceDTO>? = emptyList(),
    // Details
    @SerialName("provenance") val provenance: String? = null,
    @SerialName("creditline") val creditLine: String? = null,
    @SerialName("classification") val classification: String? = null,
    @SerialName("century") val century: String? = null,
    @SerialName("culture") val culture: String? = null,
    @SerialName("medium") val medium: String? = null, // "Ink on paper"
    @SerialName("period") val period: String? = null, // "Edo period"
    @SerialName("style") val style: String? = null, // "Shijo"
    @SerialName("dimensions") val dimensions: String? = null, // "20 x 30 cm"
    @SerialName("copyright") val copyright: String? = null,
    @SerialName("gallery") val gallery: GalleryDTO? = null,
)

@Serializable
data class PersonDTO(
    @SerialName("displayname") val name: String? = null,
    @SerialName("role") val role: String? = null, // "Artist", "Maker"
)

@Serializable
data class ImageDTO(
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0,
    @SerialName("baseimageurl") val url: String? = null,
)

@Serializable
data class PlaceDTO(
    @SerialName("displayname") val name: String? = null, // "Paris, France"
)

@Serializable
data class GalleryDTO(
    @SerialName("gallerynumber") val number: String? = null,
    @SerialName("name") val name: String? = null,
)
