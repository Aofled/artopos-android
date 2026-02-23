@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package ru.createsmart.artopos.feature.discover.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.FilterListItem
import ru.createsmart.artopos.feature.discover.model.FiltersUiState

private const val TIGHT_SCREEN_THRESHOLD_RATIO = 0.75f
private const val FILTER_GRID_ROWS = 3
private val FILTER_GRID_HEIGHT = 130.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    filtersState: FiltersUiState,
    onFilterSelected: (FilterType, String?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val estimatedContentHeight = 600.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        BoxWithConstraints {
            val screenHeight = maxHeight

            // UX Logic: Check if content fits comfortably on the screen.
            // If it takes >75% of space (e.g. Landscape mode or small phone),
            // force the Sheet to expand to full height for better scrolling.
            val isTightScreen =
                estimatedContentHeight > (screenHeight * TIGHT_SCREEN_THRESHOLD_RATIO)

            Column(
                modifier = Modifier
                    .then(if (isTightScreen) Modifier.fillMaxHeight() else Modifier)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
                    .padding(WindowInsets.navigationBars.asPaddingValues()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.title_filters),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(onClick = onReset) {
                        Text(stringResource(R.string.btn_reset))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FilterHorizontalGridSection(
                    title = stringResource(R.string.filter_classification),
                    items = filtersState.classifications,
                    type = FilterType.CLASSIFICATION,
                    onSelect = onFilterSelected,
                )

                FilterHorizontalGridSection(
                    title = stringResource(R.string.filter_century),
                    items = filtersState.centuries,
                    type = FilterType.CENTURY,
                    onSelect = onFilterSelected,
                )

                FilterHorizontalGridSection(
                    title = stringResource(R.string.filter_culture),
                    items = filtersState.cultures,
                    type = FilterType.CULTURE,
                    onSelect = onFilterSelected,
                )
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(
    title: String,
    selectedItemName: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp),
        )

        AnimatedVisibility(
            visible = selectedItemName != null,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            if (selectedItemName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = selectedItemName,
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
}

@Composable
private fun FilterHorizontalGridSection(
    title: String,
    items: List<FilterListItem>,
    type: FilterType,
    onSelect: (FilterType, String?) -> Unit,
) {
    if (items.isEmpty()) return

    val selectedItem = items.find { it.isSelected }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        FilterSectionHeader(
            title = title,
            selectedItemName = selectedItem?.name,
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
                    isSelected = items.none { it.isSelected },
                    onClick = { onSelect(type, null) },
                )
            }

            items(items = items, key = { it.id }) { item ->
                FilterChip(
                    selected = item.isSelected,
                    onClick = {
                        val newValue = if (item.isSelected) null else item.name
                        onSelect(type, newValue)
                    },
                    label = { Text("${item.name} (${item.count})") },
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
        label = {
            Text(
                text = stringResource(R.string.item_any),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    )
}
