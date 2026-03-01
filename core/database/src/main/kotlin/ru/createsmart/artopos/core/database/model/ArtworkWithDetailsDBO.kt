package ru.createsmart.artopos.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class ArtworkWithDetailsDBO(
    @Embedded val artwork: ArtworkDBO,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
    )
    val details: ArtworkDetailsDBO?, // Be null if there are no details
)
