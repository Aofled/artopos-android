package ru.createsmart.artopos.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val lightColorScheme = lightColorScheme(
    primary = NavyDark,
    onPrimary = PaperWhite,
    primaryContainer = NavyLight,
    onPrimaryContainer = PaperWhite,

    secondary = GoldAccent,
    onSecondary = NavyDark,

    background = PaperWhite,
    onBackground = NavyDark,

    surface = PaperWhite,
    onSurface = NavyDark,

    error = ErrorRed,
)

private val darkColorScheme = darkColorScheme(
    primary = PaperWhite,
    onPrimary = NavyDark,

    secondary = GoldAccent,
    onSecondary = NavyDark,

    background = MapWater,
    onBackground = PaperWhite,

    surface = MapLand,
    onSurface = PaperWhite,
)

@Composable
fun ArtoposTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep 'false' to enforce Brand Colors (Navy/Gold) instead of User's Wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Fix Status Bar icons color
            // Dark Theme -> Light Icons (isAppearanceLightStatusBars = false)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArtoposTypography,
        content = content,
    )
}
