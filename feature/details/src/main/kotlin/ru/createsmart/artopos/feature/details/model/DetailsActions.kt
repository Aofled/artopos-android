package ru.createsmart.artopos.feature.details.model

import ru.createsmart.artopos.core.designsystem.components.UiText

data class DetailsActions(
    val onShowMessage: (UiText) -> Unit,
    val onToggleTranslation: () -> Unit,
    val onFavoriteClick: () -> Unit,
)
