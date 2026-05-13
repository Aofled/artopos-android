package ru.createsmart.artopos.core.navigation

import kotlinx.serialization.Serializable

@Serializable
public object DiscoverRoute

@Serializable
public object FavoritesRoute

@Serializable
public object SettingsRoute

@Serializable
public data class DetailsRoute(
    val artworkId: Int,
)
