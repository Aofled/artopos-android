package ru.createsmart.artopos.core.uicomponents.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.createsmart.artopos.core.designsystem.R

@Composable
fun FilterFloatingActionButton(
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onFilterClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_nav_filter_list),
            contentDescription = stringResource(R.string.core_cd_retry),
        )
    }
}
