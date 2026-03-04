package ru.createsmart.artopos.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object DiscoverRoute

@Serializable
data class DetailsRoute(
    val artworkId: Int,
)
