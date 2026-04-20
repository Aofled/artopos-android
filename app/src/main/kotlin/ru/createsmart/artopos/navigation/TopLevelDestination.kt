package ru.createsmart.artopos.navigation

import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.navigation.SettingsRoute
import ru.createsmart.artopos.core.designsystem.R as DSR

enum class TopLevelDestination(
    val route: Any,
    val iconUnselected: Int,
    val iconSelected: Int,
    val titleTextId: Int,
) {
    DISCOVER(
        route = DiscoverRoute,
        iconUnselected = DSR.drawable.ic_nav_home_outlined,
        iconSelected = DSR.drawable.ic_nav_home_filled,
        titleTextId = DSR.string.core_title_discover,
    ),
    FAVORITES(
        route = FavoritesRoute,
        iconUnselected = DSR.drawable.ic_nav_heart_outlined,
        iconSelected = DSR.drawable.ic_nav_heart_filled,
        titleTextId = DSR.string.core_title_favorites,
    ),
    SETTINGS(
        route = SettingsRoute,
        iconUnselected = DSR.drawable.ic_nav_settings_outlined,
        iconSelected = DSR.drawable.ic_nav_settings_filled,
        titleTextId = DSR.string.core_title_settings,
    ),
}
