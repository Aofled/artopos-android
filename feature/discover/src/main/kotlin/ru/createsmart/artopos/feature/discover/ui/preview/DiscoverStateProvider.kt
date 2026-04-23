package ru.createsmart.artopos.feature.discover.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText

class DiscoverStateProvider : PreviewParameterProvider<Flow<PagingData<ArtworkListItem>>> {
    override val values: Sequence<Flow<PagingData<ArtworkListItem>>> = sequenceOf(
        // Successful loading with data
        flowOf(
            PagingData.from(
                data = listOf(
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
                        isFavorite = false,
                    ),
                    ArtworkListItem(
                        id = 3,
                        title = UiText.DynamicString("Grasses and Moon"),
                        artist = UiText.DynamicString("Tani Bunchō 谷文晁"),
                        imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:LEG252388",
                        aspectRatio = 1.9600308f,
                        year = "15th day of the 8th month, 1817",
                        isFavorite = false,
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
                    ArtworkListItem(
                        id = 5,
                        title = UiText.DynamicString("Chinese Roses and Sweetfish"),
                        artist = UiText.DynamicString("Ueda Kōchū 上田耕冲"),
                        imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765896",
                        aspectRatio = 0.4043137f,
                        year = "1897",
                        isFavorite = false,
                    ),
                    ArtworkListItem(
                        id = 6,
                        title = UiText.DynamicString("Rushes and Dragonflies"),
                        artist = UiText.DynamicString("Watanabe Seitei 渡辺省亭"),
                        imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:766941",
                        aspectRatio = 0.37176472f,
                        year = "c. 1893-1917",
                        isFavorite = true,
                    ),
                ),
            ),
        ),

        // Loading state
        flowOf(
            PagingData.from(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(false),
                    append = LoadState.NotLoading(false),
                ),
            ),
        ),

        // ERROR state
        flowOf(
            PagingData.from(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Error(Exception("No internet")),
                    prepend = LoadState.NotLoading(false),
                    append = LoadState.NotLoading(false),
                ),
            ),
        ),
    )
}
