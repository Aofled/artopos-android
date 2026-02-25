package ru.createsmart.artopos.feature.discover.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.FilterListItem
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import ru.createsmart.artopos.feature.discover.ui.preview.FilterPreviewData
import ru.createsmart.artopos.feature.discover.util.FilterNameHelper
import ru.createsmart.artopos.core.ui.R as UiR

private val FILTER_GRID_HEIGHT = 135.dp
private val FILTER_SECTION_HEADER_HEIGHT = 50.dp
private val FILTER_HEADER_HEIGHT = 60.dp
private const val TIGHT_SCREEN_THRESHOLD_RATIO = 0.75f
private const val FILTER_GRID_ROWS = 3

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    filtersState: FiltersUiState,
    onFilterSelected: (FilterType, String?) -> Unit,
    onReset: () -> Unit,
    onToggleSort: () -> Unit,
    onDismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current

    @Suppress("NewApi")
    val screenHeightDp = configuration.screenHeightDp.dp

    val calculatedContentHeight = remember { // Calculate the content height
        val sectionHeight = FILTER_GRID_HEIGHT + FILTER_SECTION_HEADER_HEIGHT
        val headerHeight = FILTER_HEADER_HEIGHT
        (sectionHeight * FILTER_GRID_ROWS) + headerHeight
    }

    // Minimum height is 75%, after which the "spring effect" begins
    val isTightScreen = remember(screenHeightDp) {
        calculatedContentHeight > (screenHeightDp * TIGHT_SCREEN_THRESHOLD_RATIO) //
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .then(if (isTightScreen) Modifier.fillMaxHeight() else Modifier) // To avoid the "spring effect"
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
                .padding(WindowInsets.navigationBars.asPaddingValues()),
        ) {
            FilterSheetHeader(
                onReset = onReset,
                sort = filtersState.sort,
                onToggleSort = onToggleSort,
            )
            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current

            FilterSection(
                context = context,
                title = stringResource(R.string.filter_classification),
                items = filtersState.classifications,
                type = FilterType.CLASSIFICATION,
                onSelect = onFilterSelected,
            )

            FilterSection(
                context = context,
                title = stringResource(R.string.filter_century),
                items = filtersState.centuries,
                type = FilterType.CENTURY,
                onSelect = onFilterSelected,
            )

            FilterSection(
                context = context,
                title = stringResource(R.string.filter_culture),
                items = filtersState.cultures,
                type = FilterType.CULTURE,
                onSelect = onFilterSelected,
            )
        }
    }
}

@Composable
private fun FilterSheetHeader(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    sort: FilterSortOption,
    onToggleSort: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.title_filters),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.btn_reset))
            }

            val containerColor = when (sort) {
                FilterSortOption.RANK -> MaterialTheme.colorScheme.secondaryContainer
                FilterSortOption.TOTAL_PAGE_VIEWS -> MaterialTheme.colorScheme.tertiaryContainer
                FilterSortOption.RANDOM -> MaterialTheme.colorScheme.primaryContainer
            }

            FilledTonalIconButton(
                onClick = onToggleSort,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = containerColor,
                ),
            ) {
                AnimatedContent(targetState = sort, label = "sort_icon") { targetSort ->
                    val iconRes = when (targetSort) {
                        FilterSortOption.RANK -> UiR.drawable.sort
                        FilterSortOption.TOTAL_PAGE_VIEWS -> UiR.drawable.person_heart
                        FilterSortOption.RANDOM -> UiR.drawable.shuffle
                    }
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(R.string.sort_filters),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    context: Context,
    title: String,
    items: List<FilterListItem>,
    type: FilterType,
    onSelect: (FilterType, String?) -> Unit,
) {
    if (items.isEmpty()) return

    val selectedItem = remember(items) { items.find { it.isSelected } }

    val localizedHeaderName = remember(selectedItem) {
        selectedItem?.name?.let { FilterNameHelper.getLocalizedName(context, it) }
    }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        SectionHeaderTitle(
            title = title,
            selectedLocalizedName = localizedHeaderName,
        )

        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(FILTER_GRID_ROWS),
            modifier = Modifier
                .height(FILTER_GRID_HEIGHT)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalItemSpacing = 8.dp,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AnyFilterChip(
                    isSelected = selectedItem == null,
                    onClick = { onSelect(type, null) },
                )
            }

            items(items = items, key = { it.id }) { item ->
                val itemNameLocalized = remember(item.name) {
                    FilterNameHelper.getLocalizedName(context, item.name)
                }

                FilterChip(
                    selected = item.isSelected,
                    onClick = {
                        val newValue = if (item.isSelected) null else item.name
                        onSelect(type, newValue)
                    },
                    label = { Text(text = itemNameLocalized) }, // "$itemNameLocalized (${item.count})",
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderTitle(
    title: String,
    selectedLocalizedName: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 8.dp),
        )

        AnimatedVisibility(
            visible = selectedLocalizedName != null,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                Text(
                    text = selectedLocalizedName.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AnyFilterChip(isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = stringResource(R.string.item_any)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outline,
            borderWidth = 1.dp,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun FilterBottomSheetPreview() {
    val previewState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ArtoposTheme {
        FilterBottomSheet(
            sheetState = previewState,
            filtersState = FiltersUiState(
                classifications = FilterPreviewData.classifications,
                centuries = FilterPreviewData.centuries,
                cultures = FilterPreviewData.cultures,
                isAvailable = true,
                sort = FilterSortOption.RANK,
            ),
            onFilterSelected = { _, _ -> },
            onReset = { },
            onToggleSort = { },
            onDismiss = { },
        )
    }
}
