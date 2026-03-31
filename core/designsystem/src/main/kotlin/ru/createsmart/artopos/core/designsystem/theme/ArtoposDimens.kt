package ru.createsmart.artopos.core.designsystem.theme

import androidx.compose.ui.unit.dp

object ArtoposDimens {

    const val BOTTOM_BAR_ANIMATION_DURATION = 400

    val BottomBarHeight = 48.dp
    val CircularProgressIndicatorHeight = 48.dp

    // --- Component-specific indentations ---

    // Indent for Snackbar when there is no menu and FAB
    val SnackbarPaddingWithMenu = 52.dp

    // Indent for Snackbar when menu is not hidden but FAB is present
    val SnackbarPaddingFabWithMenu = 122.dp

    // Indent for Snackbar when menu is hidden but FAB is present
    val SnackbarPaddingFabWithoutMenu = 74.dp
}
