package ru.createsmart.artopos.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO for gluing Artwork (Parent) and Details (Child).
 */
data class ArtworkWithDetailsDBO(
    @Embedded val artwork: ArtworkDBO,

    @Relation(
        parentColumn = "id", // Field 'id' in table 'artwork'
        entityColumn = "id", // Field 'id' in table 'artwork_details'
    )
    val details: ArtworkDetailsDBO?, // Be null if there are no details
)
