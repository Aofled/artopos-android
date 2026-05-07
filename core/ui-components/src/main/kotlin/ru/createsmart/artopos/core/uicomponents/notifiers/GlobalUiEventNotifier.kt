package ru.createsmart.artopos.core.uicomponents.notifiers

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Global UI events sent from the Scaffold (App) level
 * down to specific screens (Features).
 */
sealed interface GlobalUiEvent {
    /**
     * Signals the screen to scroll its main list to the top.
     * Sent when the active tab in the BottomBar is clicked again.
     */
    data class ScrollToTop(val route: String) : GlobalUiEvent
}

/**
 * A simple Event Bus for passing events from the BottomBar to screens within the NavHost.
 */
class GlobalUiEventBus {
    private val _events = MutableSharedFlow<GlobalUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GlobalUiEvent> = _events

    fun sendEvent(event: GlobalUiEvent) {
        _events.tryEmit(event)
    }
}

val LocalGlobalUiEventBus = compositionLocalOf<GlobalUiEventBus> {
    error("GlobalUiEventBus not provided")
}
