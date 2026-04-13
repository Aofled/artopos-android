package ru.createsmart.artopos.core.uicomponents.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
) {
    InputChip(
        selected = selected,
        onClick = onClick,
        label = label,
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_close),
                contentDescription = stringResource(R.string.core_cd_remove_filter),
                modifier = Modifier.size(16.dp),
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = true,
            borderColor = MaterialTheme.colorScheme.outline,
            borderWidth = 1.dp,
        ),
    )
}
