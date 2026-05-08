package ru.createsmart.artopos.feature.favorites.model

sealed interface FavoritesIntent {
    data object Refresh : FavoritesIntent
    data class ToggleFavorite(val id: Int) : FavoritesIntent
}
