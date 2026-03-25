package ru.createsmart.artopos.core.uicomponents.manager

import UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.createsmart.artopos.core.common.util.NetworkMonitor
import ru.createsmart.artopos.core.designsystem.R
import javax.inject.Inject

private const val ERROR_DEBOUNCE_MS = 3000L

class UiMessageManager @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) {
    private val _uiEffect = Channel<UiText>(Channel.BUFFERED) // For Snackbar
    val uiEffect = _uiEffect.receiveAsFlow()

    private var lastEmittedMessage: UiText? = null
    private var lastEmittedTime: Long = 0L

    fun resetLastEmittedMessage() {
        lastEmittedMessage = null
    }

    fun checkInternetAndNotify(): Boolean {
        if (!networkMonitor.isOnline()) {
            sendSideEffect(UiText.StringResource(R.string.error_no_internet))
            return false
        }
        return true
    }

    fun sendSideEffect(message: UiText) {
        val currentTime = System.currentTimeMillis()

        // Debounce Logic: prevent spamming the user with identical Snackbars (e.g. multiple image failures at once).
        if (message == lastEmittedMessage && (currentTime - lastEmittedTime) < ERROR_DEBOUNCE_MS) {
            return
        }
        lastEmittedMessage = message
        lastEmittedTime = currentTime
        _uiEffect.trySend(message)
    }
}
