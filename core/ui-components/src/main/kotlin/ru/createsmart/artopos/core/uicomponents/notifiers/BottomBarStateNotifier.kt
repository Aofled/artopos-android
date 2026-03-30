package ru.createsmart.artopos.core.uicomponents.notifiers

import androidx.compose.runtime.compositionLocalOf

// This is the channel through which the list says: "End of list!" (Needed for navigation)
val LocalBottomBarStateNotifier = compositionLocalOf<(Boolean) -> Unit> {
    {
            _ ->
    }
}

val LocalBottomBarVisibility = compositionLocalOf<Boolean> { true }
