package ru.createsmart.artopos.feature.details.model

import UiText

data class DetailsActions(
    val onShowMessage: (UiText) -> Unit,
    val onToggleTranslation: () -> Unit,
)
