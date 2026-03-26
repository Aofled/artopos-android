package ru.createsmart.artopos.core.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.uicomponents.R
import ru.createsmart.artopos.core.designsystem.R as UiR

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = if (isFullScreen) 0.3f else 0.2f),
                shape = RoundedCornerShape(12.dp),
            )
            .size(if (isFullScreen) 40.dp else 36.dp),
    ) {
        val iconRes = if (isFavorite) {
            UiR.drawable.favorite
        } else {
            UiR.drawable.favorite_border
        }

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = if (isFavorite) {
                stringResource(R.string.description_remove_favorites)
            } else {
                stringResource(R.string.description_add_favorites)
            },
            tint = Color.White,
            modifier = Modifier
                .size(if (isFullScreen) 24.dp else 20.dp),
        )
    }
}
