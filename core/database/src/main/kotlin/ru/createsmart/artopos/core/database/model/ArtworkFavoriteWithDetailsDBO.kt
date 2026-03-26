package ru.createsmart.artopos.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO for gluing Favorites (Parent) and Details (Child).
 */
data class ArtworkFavoriteWithDetailsDBO(
    @Embedded val favoriteArtwork: ArtworkFavoriteDBO,

    @Relation(
        parentColumn = "id", // Field 'id' in table 'favorites'
        entityColumn = "id", // Field 'id' in table 'artwork_details'
    )
    val details: ArtworkDetailsDBO?, // Be null if there are no details
)
