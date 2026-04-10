package ru.createsmart.artopos.core.uicomponents.notifiers

import androidx.compose.runtime.compositionLocalOf

// This is the channel through which the list says: "End of list!" (Needed for navigation)
val LocalBottomBarStateNotifier = compositionLocalOf<(BottomBarCommand) -> Unit> {
    {
            _ ->
    }
}

val LocalBottomBarVisibility = compositionLocalOf<Boolean> { true }

enum class BottomBarCommand {
    SHOW, // Show menu (used when pressing buttons or tabs)
    LOCK_AT_BOTTOM, // Bottom of the list: show the menu and lock it from hiding
    UNLOCK, // Dropped from the bottom of the list: Unblock
}
