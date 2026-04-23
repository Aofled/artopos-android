package ru.createsmart.artopos.feature.details.model

import ru.createsmart.artopos.core.designsystem.components.UiText

sealed interface DetailsIntent {
    data object Refresh : DetailsIntent
    data object ToggleFavorite : DetailsIntent
    data object ToggleTranslation : DetailsIntent
    data class DownloadImage(val url: String, val title: String) : DetailsIntent
    data class ShowMessage(val message: UiText) : DetailsIntent
}
