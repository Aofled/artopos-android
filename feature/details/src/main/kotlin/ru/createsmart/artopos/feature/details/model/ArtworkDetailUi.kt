package ru.createsmart.artopos.feature.details.model

import UiText
import androidx.compose.runtime.Immutable

@Immutable
data class ArtworkDetailUi(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val description: String?,
    val webUrl: String?,
    val details: List<DetailItem>,
    val images: List<GalleryImageUi> = emptyList(),
    val copyright: String? = null,
    val date: String?,
    val culture: String?,
)

@Immutable
data class DetailItem(
    val label: UiText,
    val value: String,
    val isWide: Boolean = false,
)

@Immutable
data class GalleryImageUi(
    val url: String,
    val aspectRatio: Float,
)
