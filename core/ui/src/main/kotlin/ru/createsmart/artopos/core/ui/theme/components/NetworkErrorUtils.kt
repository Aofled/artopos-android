package ru.createsmart.artopos.core.ui.theme.components

import UiText
import ru.createsmart.artopos.core.ui.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUiText(): UiText {
    return when (this) {
        // Specific network errors (No DNS / No Internet)
        is UnknownHostException -> UiText.StringResource(R.string.error_failed_connect)

        // Slow connection
        is SocketTimeoutException -> UiText.StringResource(R.string.error_timed_out)

        // Generic Network IO error (must be checked AFTER specific subclasses like SocketTimeoutException)
        is IOException -> UiText.StringResource(R.string.error_network_error)

        // Fallback: Any other crash (NPE, JsonParsing, etc.) -> "Server Error"
        else -> UiText.StringResource(R.string.error_server)
    }
}
