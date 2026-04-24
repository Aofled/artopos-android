package ru.createsmart.artopos.feature.details.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.feature.details.ArtworkDetailUiState
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.model.DetailItem
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

class ArtworkDetailStateProvider : PreviewParameterProvider<ArtworkDetailUiState> {

    override val values: Sequence<ArtworkDetailUiState> = sequenceOf(
        ArtworkDetailUiState.Loading,

        ArtworkDetailUiState.Error,

        ArtworkDetailUiState.Success(
            artwork = artworkPreview,
        ),
    )
}

val artworkPreview = ArtworkDetailUi(
    id = 357597,
    title = "Blossoming Plum with Moon and Snow (left scroll)",
    artist = "Goshun 呉春",
    imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765757",
    description = "Pair of scrolls depicting blossoming plum trees. " +
        " The right scroll depicts an old plum tree covered lightly in snow." +
        " Both images are juxtaposed against a light gray wash indicating early evening.",
    classification = "Paintings",
    century = "18 th -19 th century",
    culture = "Japanese",
    webUrl = "https://www.harvardartmuseums.org/collections/object/357597",
    details = persistentListOf(
        DetailItem(
            label = UiText.StringResource(R.string.details_label_period),
            value = "Edo period 1615 - 1868",
            isWide = false,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_style),
            value = "Shijo",
            isWide = false,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_classification),
            value = "Paintings",
            isWide = false,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_date),
            value = "late 18th-early 19th century",
            isWide = false,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_medium),
            value = "Left scroll of a pair of hanging scrolls; ink and light color on paper.",
            isWide = true,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_dimensions),
            value = "H.136.8 × W . 63.6 cm (53 7 / 8 × 25 1 / 16 in .)" +
                " overall mounting, including roller ends and suspension cord:" +
                " H. 198.5 × W. 71 cm (78 1/8 × 27 15/16 in .)",
            isWide = true,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_gallery),
            value = "East Asian Art",
            isWide = true,
        ),
        DetailItem(
            label = UiText.StringResource(R.string.details_label_credit_line),
            value = "Promised gift of Robert S.and Betsy G.Feinberg",
            isWide = true,
        ),
    ),
    images = persistentListOf(
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765757",
            aspectRatio = 0.45686275f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765752",
            aspectRatio = 0.75333333f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765753",
            aspectRatio = 0.6282353f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765754",
            aspectRatio = 0.652549f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765755",
            aspectRatio = 0.5015686f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765756",
            aspectRatio = 0.60039216f,
        ),
        GalleryImageUi(
            url = "https://nrs.harvard.edu/urn-3:HUAM:765758",
            aspectRatio = 0.3737255f,
        ),
    ),
    copyright = null,
    isFavorite = true,
)
