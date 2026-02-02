package ru.createsmart.artopos.feature.discover.model

import androidx.compose.runtime.Immutable

@Immutable
data class ArtworkListItem(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val aspectRatio: Float,
    val year: String,
)
