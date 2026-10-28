package ru.createsmart.artopos.core.uicomponents.notifiers

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.SharedFlow

val LocalBottomBarStateNotifier = compositionLocalOf<(BottomBarCommand) -> Unit> { { _ -> } }
val LocalBottomBarVisibility = compositionLocalOf<Boolean> { true }

// Emits route object when tab is reselected
val LocalTabReselectFlow = compositionLocalOf<SharedFlow<Any>> {
    error("No LocalTabReselectFlow provided")
}

enum class BottomBarCommand {
    SHOW, // Show menu (used when pressing buttons or tabs)
    LOCK_AT_BOTTOM, // Bottom of the list: show the menu and lock it from hiding
    UNLOCK, // Dropped from the bottom of the list: Unblock
}
