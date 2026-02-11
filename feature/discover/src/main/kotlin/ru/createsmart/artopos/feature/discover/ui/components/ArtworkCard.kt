package ru.createsmart.artopos.feature.discover.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

@Composable
fun ArtworkCard(
    modifier: Modifier = Modifier,
    artwork: ArtworkListItem,
    contentVersion: Int,
    onClick: () -> Unit,
    onShowMessage: (UiText) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        ArtworkImage(artwork, contentVersion, onShowMessage)
        Column(
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        ) {
            Text(
                text = artwork.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (artwork.artist.isNotBlank()) {
                Text(
                    text = artwork.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
            ),
            onClick = {},
            onShowMessage = {},
            contentVersion = 0,
            modifier = Modifier.padding(16.dp),
        )
    }
}
