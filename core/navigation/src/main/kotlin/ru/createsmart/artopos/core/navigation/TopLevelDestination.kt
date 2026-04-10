package ru.createsmart.artopos.core.navigation

import ru.createsmart.artopos.core.designsystem.R as UiR

enum class TopLevelDestination(
    val route: Any,
    val iconUnselected: Int,
    val iconSelected: Int,
    val titleTextId: Int,
) {
    DISCOVER(
        route = DiscoverRoute,
        iconUnselected = UiR.drawable.ic_nav_home_outlined,
        iconSelected = UiR.drawable.ic_nav_home_filled,
        titleTextId = UiR.string.title_discover,
    ),
    FAVORITES(
        route = FavoritesRoute,
        iconUnselected = UiR.drawable.ic_nav_heart_outlined,
        iconSelected = UiR.drawable.ic_nav_heart_filled,
        titleTextId = UiR.string.title_favorites,
    ),
    SETTINGS(
        route = SettingsRoute,
        iconUnselected = UiR.drawable.ic_nav_settings_outlined,
        iconSelected = UiR.drawable.ic_nav_settings_filled,
        titleTextId = UiR.string.title_settings,
    ),
}
