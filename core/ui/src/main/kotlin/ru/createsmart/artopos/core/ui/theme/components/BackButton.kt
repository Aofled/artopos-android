package ru.createsmart.artopos.core.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.ui.R

@Composable
fun BackButton(
    onBackClick: () -> Unit,
) {
    IconButton(
        onClick = onBackClick,
        modifier = Modifier
            .statusBarsPadding()
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = stringResource(R.string.description_btn_back),
            tint = Color.White,
        )
    }
}
