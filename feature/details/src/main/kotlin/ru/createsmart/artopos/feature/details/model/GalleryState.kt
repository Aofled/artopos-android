package ru.createsmart.artopos.feature.details.model

import androidx.compose.foundation.pager.PagerState

data class GalleryState(
    val pagerState: PagerState,
    val isScrolledDown: Boolean,
    val contentVersion: Int,
)
