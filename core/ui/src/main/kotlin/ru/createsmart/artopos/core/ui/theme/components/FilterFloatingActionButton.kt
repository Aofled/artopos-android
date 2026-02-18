package ru.createsmart.artopos.core.ui.theme.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.createsmart.artopos.core.ui.R

@Composable
fun FilterFloatingActionButton(
    onFilterClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onFilterClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.filter_list),
            contentDescription = stringResource(R.string.btn_retry),
        )
    }
}
