package ru.createsmart.artopos.feature.search.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.uicomponents.components.CustomInputChip
import ru.createsmart.artopos.feature.search.model.ActiveFilterItemUi

@Composable
fun ActiveFiltersRow(
    activeFilters: ImmutableList<ActiveFilterItemUi>,
    onRemoveFilter: (FilterType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        items(
            items = activeFilters,
            key = { it.type },
        ) { item ->
            CustomInputChip(
                selected = true,
                onClick = { onRemoveFilter(item.type) },
                label = { Text(item.label.asString()) },
            )
        }
    }
}
