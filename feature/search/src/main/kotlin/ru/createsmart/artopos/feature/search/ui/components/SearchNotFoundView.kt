package ru.createsmart.artopos.feature.search.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.feature.search.R

private val CYRILLIC_REGEX = Regex("[а-яА-ЯёЁ]")

@Composable
fun SearchNotFoundView(
    query: String,
    modifier: Modifier = Modifier,
) {
    val hasCyrillic = remember(query) {
        CYRILLIC_REGEX.containsMatchIn(query)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val titleRes = if (hasCyrillic) {
            R.string.search_not_found_cyrillic_title
        } else {
            R.string.search_not_found_title
        }

        val subtitleRes = if (hasCyrillic) {
            R.string.search_not_found_cyrillic_hint
        } else {
            R.string.search_not_found_subtitle
        }

        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasCyrillic) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 290.dp),
        )
    }
}
