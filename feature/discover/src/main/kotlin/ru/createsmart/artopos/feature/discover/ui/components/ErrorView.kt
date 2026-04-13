package ru.createsmart.artopos.feature.discover.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.designsystem.R as DSR

@Composable
fun ErrorView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(DSR.string.core_error_generic),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = stringResource(DSR.string.core_cd_retry),
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ErrorViewPreview() {
    ArtoposTheme {
        ErrorView(onRetry = {})
    }
}
