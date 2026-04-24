package ru.createsmart.artopos.feature.details.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.createsmart.artopos.core.designsystem.components.UiText

@Immutable
data class ArtworkDetailUi(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val description: String?,
    val webUrl: String?,
    val classification: String?,
    val century: String?,
    val culture: String?,
    val details: ImmutableList<DetailItem>,
    val images: ImmutableList<GalleryImageUi> = persistentListOf(),
    val copyright: String? = null,
    val isFavorite: Boolean,
    val isTranslated: Boolean = false,
    val canBeTranslated: Boolean = false,
    val isTranslationPending: Boolean = false,
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
