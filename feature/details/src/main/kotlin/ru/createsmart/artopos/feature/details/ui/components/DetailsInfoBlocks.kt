package ru.createsmart.artopos.feature.details.ui.components

import UiText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.createsmart.artopos.core.ui.theme.FontFamilySerif
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi

@Composable
internal fun ArtistAndTitle(
    artwork: ArtworkDetailUi,
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = artwork.artist,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artwork.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
internal fun KeyFactsRow(artwork: ArtworkDetailUi) {
    val hasFirstRow = !artwork.classification.isNullOrBlank() || !artwork.culture.isNullOrBlank()
    val hasDate = !artwork.century.isNullOrBlank()

    if (!hasFirstRow && !hasDate) return

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (hasFirstRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    if (!artwork.classification.isNullOrBlank()) {
                        FactItem(
                            labelRes = R.string.details_classification,
                            value = artwork.classification,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (!artwork.culture.isNullOrBlank()) {
                        FactItem(
                            labelRes = R.string.details_culture,
                            value = artwork.culture,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (!artwork.century.isNullOrBlank()) {
                FactItem(
                    labelRes = R.string.details_century,
                    value = artwork.century,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = 0.dp,
            ),
            color = MaterialTheme.colorScheme.outlineVariant,

        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FactItem(
    labelRes: Int,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(labelRes).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun Description(
    description: String,
) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = FontFamilySerif,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 30.sp,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
internal fun DetailsTable(
    artwork: ArtworkDetailUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        val details = artwork.details
        var i = 0

        while (i < details.size) {
            val current = details[i]
            val next = details.getOrNull(i + 1)

            if (!current.isWide && next != null && !next.isWide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DetailItemRow(current.label, current.value, Modifier.weight(1f))
                    DetailItemRow(next.label, next.value, Modifier.weight(1f))
                }
                i += 2
            } else {
                DetailItemRow(current.label, current.value, Modifier.fillMaxWidth())
                i++
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    label: UiText,
    value: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val labelString = label.asString(context)

    Column(
        modifier = modifier
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = labelString.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun CopyrightFooter(
    copyright: String,
) {
    Text(
        text = copyright,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )
}
