package ru.createsmart.artopos.feature.discover.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.FilterListItem
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import ru.createsmart.artopos.feature.discover.ui.preview.FilterPreviewData
import ru.createsmart.artopos.core.designsystem.R as DSR

private val FILTER_GRID_HEIGHT = 130.dp
private val FILTER_SECTION_HEADER_HEIGHT = 50.dp
private val FILTER_HEADER_HEIGHT = 120.dp // 60
private const val TIGHT_SCREEN_THRESHOLD_RATIO = 0.75f
private const val FILTER_GRID_ROWS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    filtersState: FiltersUiState,
    onFilterSelected: (FilterType, String?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onReset: () -> Unit,
    onToggleSort: () -> Unit,
    onDismiss: () -> Unit,
) {
    val currentContext = LocalContext.current
    val currentConfig = LocalConfiguration.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        // TODO(WORKAROUND): ModalBottomSheet creates a new window and loses the overridden LocalConfiguration.
        // We must manually provide the localized context inside the sheet's scope.
        CompositionLocalProvider(
            LocalContext provides currentContext,
            LocalConfiguration provides currentConfig,
        ) {
            FilterSheetContent(
                filtersState = filtersState,
                onFilterSelected = onFilterSelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onReset = onReset,
                onToggleSort = onToggleSort,
            )
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun FilterSheetContent(
    filtersState: FiltersUiState,
    onFilterSelected: (FilterType, String?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onReset: () -> Unit,
    onToggleSort: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp

    val isTightScreen = remember(screenHeightDp) {
        val contentHeight =
            (FILTER_GRID_HEIGHT + FILTER_SECTION_HEADER_HEIGHT) * FILTER_GRID_ROWS + FILTER_HEADER_HEIGHT
        contentHeight > (screenHeightDp * TIGHT_SCREEN_THRESHOLD_RATIO)
    }

    Column(
        modifier = Modifier
            .then(if (isTightScreen) Modifier.fillMaxHeight() else Modifier)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp)
            .navigationBarsPadding(),
    ) {
        FilterSheetHeader(onReset = onReset, sort = filtersState.sort, onToggleSort = onToggleSort)
        FilterSearchBar(query = filtersState.searchQuery, onQueryChanged = onSearchQueryChanged)

        FilterSection(
            stringResource(R.string.discover_filter_label_type),
            filtersState.classifications,
            FilterType.CLASSIFICATION,
            onFilterSelected,
        )
        FilterSection(
            stringResource(R.string.discover_filter_label_period),
            filtersState.centuries,
            FilterType.CENTURY,
            onFilterSelected,
        )
        FilterSection(
            stringResource(R.string.discover_filter_label_culture),
            filtersState.cultures,
            FilterType.CULTURE,
            onFilterSelected,
        )
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(DSR.string.core_title_filters),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.width(4.dp))

        TextButton(onClick = onReset) {
            Text(stringResource(DSR.string.core_btn_reset))
        }

        Spacer(modifier = Modifier.weight(1f))

        SortToggleButton(sort = sort, onClick = onToggleSort)
    }
}

@Composable
private fun SortToggleButton(
    sort: FilterSortOption,
    onClick: () -> Unit,
) {
    val (containerColor, contentColor) = when (sort) {
        FilterSortOption.RANK ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant

        FilterSortOption.TOTAL_PAGE_VIEWS ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        FilterSortOption.ACCESSION_YEAR ->
            MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary

        FilterSortOption.DATE_BEGIN ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer

        FilterSortOption.RANDOM ->
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = Modifier.animateContentSize(),
    ) {
        AnimatedContent(targetState = sort, label = "sort_anim") { targetSort ->
            val (iconRes, textRes) = when (targetSort) {
                FilterSortOption.RANK ->
                    DSR.drawable.ic_action_sort to R.string.discover_sort_option_rank // "Curator's Pick"
                FilterSortOption.TOTAL_PAGE_VIEWS ->
                    DSR.drawable.ic_action_person_heart to R.string.discover_sort_option_views // "Most Viewed"
                FilterSortOption.ACCESSION_YEAR ->
                    DSR.drawable.ic_action_schedule to R.string.discover_sort_option_newest // "New Arrivals"
                FilterSortOption.DATE_BEGIN ->
                    DSR.drawable.ic_action_hourglass to R.string.discover_sort_option_oldest // "Oldest First"
                FilterSortOption.RANDOM ->
                    DSR.drawable.ic_action_shuffle to R.string.discover_sort_option_random // "Shuffle"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(R.string.discover_cd_sort),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(textRes),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = DSR.drawable.ic_search),
            contentDescription = stringResource(R.string.discover_cd_search),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        SearchInputField(
            query = query,
            onQueryChanged = onQueryChanged,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.discover_search_hint),
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChanged("") },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    painter = painterResource(id = DSR.drawable.ic_action_close),
                    contentDescription = stringResource(R.string.discover_cd_clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    items: List<FilterListItem>,
    type: FilterType,
    onSelect: (FilterType, String?) -> Unit,
) {
    val selectedItem = remember(items) { items.find { it.isSelected } }
    val localizedHeaderName = selectedItem?.localizedName

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

            // Either only the selected element, or completely empty - then there will be only Any
            items(items = items, key = { it.id }) { item ->
                FilterChip(
                    selected = item.isSelected,
                    onClick = {
                        val newValue = if (item.isSelected) null else item.name
                        onSelect(type, newValue)
                    },
                    label = { Text(text = item.localizedName) },
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
        label = { Text(text = stringResource(R.string.discover_filter_value_any)) },
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
            onSearchQueryChanged = { },
            onReset = { },
            onToggleSort = { },
            onDismiss = { },
        )
    }
}
