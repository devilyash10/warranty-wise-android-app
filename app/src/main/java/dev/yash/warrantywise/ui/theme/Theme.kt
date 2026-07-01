package dev.yash.warrantywise.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary            = DeepBlue300,
    onPrimary          = DeepBlue900,
    primaryContainer   = DeepBlue800,
    onPrimaryContainer = DeepBlue100,
    secondary          = Gold,
    onSecondary        = DeepBlue900,
    secondaryContainer = GoldDark,
    onSecondaryContainer = Color(0xFFFFF8E1),
    tertiary           = SuccessGreenLight,
    background         = DarkBackground,
    surface            = DarkSurface,
    surfaceVariant     = DarkSurfaceVariant,
    onBackground       = TextOnDark,
    onSurface          = TextOnDark,
    onSurfaceVariant   = Color(0xFFB0BEC5),
    outline            = DarkOutline,
    error              = ErrorRedLight
)

private val LightColors = lightColorScheme(
    primary            = DeepBlue700,
    onPrimary          = Color.White,
    primaryContainer   = DeepBlue100,
    onPrimaryContainer = DeepBlue900,
    secondary          = GoldDark,
    onSecondary        = Color.Black,
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF3E1B00),
    tertiary           = SuccessGreen,
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = TextPrimary,
    onSurface          = TextPrimary,
    error              = ErrorRed
)

@Composable
fun WarrantyWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
