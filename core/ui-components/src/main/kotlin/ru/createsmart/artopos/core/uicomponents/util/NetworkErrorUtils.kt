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
        // Domain network error: check inner cause
        is AppError.Network -> {
            when (this.cause) {
                is UnknownHostException -> UiText.StringResource(R.string.core_error_failed_connect)
                is SocketTimeoutException -> UiText.StringResource(R.string.core_error_timed_out)
                else -> UiText.StringResource(R.string.core_error_network)
            }
        }

        // Platform network errors (for backwards compatibility)
        is UnknownHostException -> UiText.StringResource(R.string.core_error_failed_connect)
        is SocketTimeoutException -> UiText.StringResource(R.string.core_error_timed_out)
        is IOException -> UiText.StringResource(R.string.core_error_network)

        // Server errors (HTTP 404, 500, etc.)
        is AppError.ExternalSystem -> {
            if (this.code == HttpURLConnection.HTTP_NOT_FOUND) {
                UiText.StringResource(R.string.core_error_server_not_found)
            } else {
                UiText.StringResource(R.string.core_error_server)
            }
        }

        is AppError.DataFormat -> UiText.StringResource(R.string.core_error_server)

        // Fallback for unexpected errors
        else -> UiText.StringResource(R.string.core_error_server)
    }
}
