package ru.createsmart.artopos.core.uicomponents.util

import ru.createsmart.artopos.core.designsystem.R
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.error.AppError
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUiText(): UiText {
    return when (this) {
        // Specific network errors (No DNS / No Internet)
        is UnknownHostException -> UiText.StringResource(R.string.core_error_failed_connect)

        // Slow connection
        is SocketTimeoutException -> UiText.StringResource(R.string.core_error_timed_out)

        // Generic Network IO error (must be checked AFTER specific subclasses like SocketTimeoutException)
        is IOException -> UiText.StringResource(R.string.core_error_network)

        // The external system (API) responded with an error (e.g. 404, 500)
        is AppError.ExternalSystem -> {
            if (this.code == HttpURLConnection.HTTP_NOT_FOUND) {
                UiText.StringResource(R.string.core_error_server_not_found)
            } else {
                UiText.StringResource(R.string.core_error_server)
            }
        }

        is AppError.DataFormat -> UiText.StringResource(R.string.core_error_server)

        // Fallback: Any other crash (NPE, JsonParsing, etc.) -> "Server Error"
        else -> UiText.StringResource(R.string.core_error_server)
    }
}
