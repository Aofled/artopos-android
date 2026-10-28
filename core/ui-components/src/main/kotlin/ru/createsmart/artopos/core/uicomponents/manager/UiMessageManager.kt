package ru.createsmart.artopos.core.uicomponents.manager

import androidx.annotation.GuardedBy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.createsmart.artopos.core.common.util.NetworkMonitor
import ru.createsmart.artopos.core.designsystem.R
import ru.createsmart.artopos.core.designsystem.components.UiText
import javax.inject.Inject

private const val ERROR_DEBOUNCE_MS = 3000L

/**
 * Manages UI messages (Snackbar) with thread-safe debounce.
 *
 * IMPORTANT: Not a @Singleton intentionally!
 * Each ViewModel gets its own isolated instance with its own Channel
 * so messages are not intercepted by neighbor screens.
 */
class UiMessageManager @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) {
    private val _uiEffect = Channel<UiText>(Channel.BUFFERED) // For Snackbar
    val uiEffect = _uiEffect.receiveAsFlow()

    private val lock = Any()

    @GuardedBy("lock")
    private var lastEmittedMessage: UiText? = null

    @GuardedBy("lock")
    private var lastEmittedTime: Long = 0L

    fun resetLastEmittedMessage() {
        synchronized(lock) {
            lastEmittedMessage = null
            lastEmittedTime = 0L
        }
    }

    fun checkInternetAndNotify(): Boolean {
        if (!networkMonitor.isOnline()) {
            sendSideEffect(UiText.StringResource(R.string.core_error_no_internet))
            return false
        }
        return true
    }

    fun sendSideEffect(message: UiText) {
        val shouldEmit = synchronized(lock) {
            val currentTime = System.currentTimeMillis()

            // Atomic check: protects against spamming duplicate messages on parallel failures.
            if (message == lastEmittedMessage && (currentTime - lastEmittedTime) < ERROR_DEBOUNCE_MS) {
                false
            } else {
                lastEmittedMessage = message
                lastEmittedTime = currentTime
                true
            }
        }

        if (shouldEmit) {
            _uiEffect.trySend(message)
        }
    }
}
