package ru.createsmart.artopos.feature.artworkcard.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.components.FavoriteButton
import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem

@Composable
fun ArtworkCard(
    modifier: Modifier = Modifier,
    artwork: ArtworkListItem,
    contentVersion: Int,
    onClick: () -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onShowMessage: (UiText) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box {
            ArtworkImage(artwork, contentVersion, onShowMessage)

            FavoriteButton(
                isFavorite = artwork.isFavorite,
                onClick = { onFavoriteClick(artwork.id) },
                modifier = Modifier
                    .align(Alignment.BottomStart) // Upper left bottom corner
                    .padding(4.dp),
                isFullScreen = false,
            )
        }

        Column(
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        ) {
            if (artwork.artist.isNotBlank()) {
                Text(
                    text = artwork.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = artwork.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ArtworkCardPreview() {
    ArtoposTheme {
        ArtworkCard(
            artwork = ArtworkListItem(
                id = 1,
                title = "Chinese Roses and Sweetfish",
                artist = "Ueda Kōchū 上田耕冲",
                imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765896",
                aspectRatio = 0.6043137f,
                year = "1897",
                isFavorite = true,
            ),
            onClick = { },
            onFavoriteClick = { },
            onShowMessage = {},
            contentVersion = 0,
            modifier = Modifier.padding(16.dp),
        )
    }
}
