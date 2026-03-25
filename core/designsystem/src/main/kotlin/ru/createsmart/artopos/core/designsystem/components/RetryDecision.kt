package ru.createsmart.artopos.core.designsystem.components

import UiText
import android.content.Context
import coil.network.HttpException
import ru.createsmart.artopos.core.common.util.isNetworkAvailable
import ru.createsmart.artopos.core.designsystem.R
import java.net.HttpURLConnection

sealed class RetryDecision {
    data object CanRetry : RetryDecision()
    data class ShowMessage(val message: UiText) : RetryDecision()
}

fun Throwable?.isNotFound(): Boolean = (this as? HttpException)?.response?.code == HttpURLConnection.HTTP_NOT_FOUND

fun Throwable?.analyzeRetry(context: Context): RetryDecision {
    return when {
        !context.isNetworkAvailable() -> RetryDecision.ShowMessage(UiText.StringResource(R.string.error_no_internet))
        this.isNotFound() -> RetryDecision.ShowMessage(UiText.StringResource(R.string.error_not_found))
        else -> RetryDecision.CanRetry
    }
}
