@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtworkDTO(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "Untitled", // Fallback: Use "Untitled" if title is missing/null
    @SerialName("dated") val date: String? = null, // "1889", "17th century"
    @SerialName("technique") val technique: String? = null, // "Oil on canvas"
    @SerialName("primaryimageurl") val imageUrl: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val webUrl: String? = null, // Link to museum site
    @SerialName("people") val artists: List<PersonDTO>? = null,
    @SerialName("images") val images: List<ImageDTO>? = null,
    @SerialName("places") val places: List<PlaceDTO>? = null,
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
