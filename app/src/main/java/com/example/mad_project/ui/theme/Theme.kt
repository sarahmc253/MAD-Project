package com.example.mad_project.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text

private val LightColourScheme = lightColorScheme(
    primary = ShelfScanGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = ShelfScanGreenLightBg,
    onPrimaryContainer = ShelfScanGreen,
    secondary = ShelfScanGreenLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    error = ExpiredRed,
    onError = androidx.compose.ui.graphics.Color.White,
    background = androidx.compose.ui.graphics.Color.White,
    onBackground = TextPrimary,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE0E0E0)
)

private val DarkColourScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = ShelfScanGreen,
    onPrimaryContainer = Color.White,
    secondary = ShelfScanGreenLight,
    onSecondary = Color.Black,
    error = ExpiredRed,
    onError = Color.White,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun MADProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour is available on Android 12+
    dynamicColour: Boolean = false,
    content: @Composable () -> Unit
) {
    val colourScheme = when {
        !dynamicColour -> if (darkTheme) DarkColourScheme else LightColourScheme
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> if (darkTheme) DarkColourScheme else LightColourScheme
    }

    MaterialTheme(
        colorScheme = colourScheme,
        typography = Typography,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun MADProjectThemePreview() {
    MADProjectTheme(dynamicColour = false) {
        Text("Theme preview", color = MaterialTheme.colorScheme.onBackground)
    }
}