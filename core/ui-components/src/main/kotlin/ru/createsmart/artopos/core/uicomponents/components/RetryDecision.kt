package ru.createsmart.artopos.core.uicomponents.components

import android.content.Context
import ru.createsmart.artopos.core.common.util.isNetworkAvailable
import ru.createsmart.artopos.core.designsystem.R
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.error.AppError
import java.net.HttpURLConnection

sealed class RetryDecision {
    data object CanRetry : RetryDecision()
    data class ShowMessage(val message: UiText) : RetryDecision()
}

fun Throwable?.isNotFound(): Boolean {
    return when (this) {
        // Error loading image via Coil
        is coil.network.HttpException -> this.response.code == HttpURLConnection.HTTP_NOT_FOUND
        // Error loading data via API (Our domain exception)
        is AppError.ExternalSystem -> this.code == HttpURLConnection.HTTP_NOT_FOUND
        else -> false
    }
}

fun Throwable?.analyzeRetry(context: Context): RetryDecision {
    return when {
        !context.isNetworkAvailable() -> RetryDecision.ShowMessage(
            UiText.StringResource(R.string.core_error_no_internet),
        )
        this.isNotFound() -> RetryDecision.ShowMessage(UiText.StringResource(R.string.core_error_server_not_found))

        else -> RetryDecision.CanRetry
    }
}
