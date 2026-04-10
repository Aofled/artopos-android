package ru.createsmart.artopos.core.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.uicomponents.R
import ru.createsmart.artopos.core.designsystem.R as UiR

@Composable
fun DownloadButton(
    artworkTitle: String,
    currentUrl: String,
    onDownloadClick: (String, String) -> Unit,
) {
    IconButton(
        onClick = {
            onDownloadClick(currentUrl, artworkTitle)
        },
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .size(40.dp),
    ) {
        Icon(
            painter = painterResource(id = UiR.drawable.ic_action_download),
            contentDescription = stringResource(R.string.description_download),
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
