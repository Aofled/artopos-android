package ru.createsmart.artopos.core.database.model

import androidx.room.Embedded

data class ArtworkWithFavoriteFlagDBO(
    @Embedded val artwork: ArtworkDBO,
    val isFavorite: Boolean,
)

data class ArtworkDetailsWithFavoriteFlagDBO(
    @Embedded val artworkWithDetails: ArtworkWithDetailsDBO,
    val isFavorite: Boolean,
)
