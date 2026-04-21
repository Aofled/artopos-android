package ru.createsmart.artopos.feature.favorites.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.feature.favorites.FavoritesUiState

class FavoritesStateProvider : PreviewParameterProvider<FavoritesUiState> {
    override val values: Sequence<FavoritesUiState> = sequenceOf(

        FavoritesUiState.Success(
            artworks = listOf(
                ArtworkListItem(
                    id = 1,
                    title = UiText.DynamicString("Egrets and Kingfisher amongst Lotus"),
                    artist = UiText.DynamicString("Okamoto Shūki 岡本秋暉"),
                    imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:LEG252412",
                    aspectRatio = 0.44163266f,
                    year = "19th century",
                    isFavorite = true,
                ),
                ArtworkListItem(
                    id = 2,
                    title = UiText.DynamicString("Shirabyōshi"),
                    artist = UiText.DynamicString("Unknown Artist"),
                    imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:763750",
                    aspectRatio = 0.43098038f,
                    year = "mid-17th century",
                    isFavorite = true,
                ),
                ArtworkListItem(
                    id = 3,
                    title = UiText.DynamicString("Grasses and Moon"),
                    artist = UiText.DynamicString("Tani Bunchō 谷文晁"),
                    imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:LEG252388",
                    aspectRatio = 1.9600308f,
                    year = "15th day of the 8th month, 1817",
                    isFavorite = true,
                ),
                ArtworkListItem(
                    id = 4,
                    title = UiText.DynamicString("Waterfall"),
                    artist = UiText.DynamicString("Mori Ippō 森一鳳"),
                    imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:768168",
                    aspectRatio = 0.5086275f,
                    year = "19th century",
                    isFavorite = true,
                ),
            ),
        ),

        FavoritesUiState.Empty,
    )
}
