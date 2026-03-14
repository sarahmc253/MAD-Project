package com.example.mad_project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text

private val AppColourScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    primaryContainer = ShelfScanGreenLightBg,
    onPrimaryContainer = AppPrimary,
    secondary = ShelfScanGreenLight,
    onSecondary = Color.White,
    error = ExpiredRed,
    onError = Color.White,
    background = AppSurface,
    onBackground = AppOnSurface,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    outline = AppOutline
)

@Composable
fun MADProjectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColourScheme,
        typography = Typography,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun MADProjectThemePreview() {
    MADProjectTheme {
        Text("Theme preview", color = MaterialTheme.colorScheme.onSurface)
    }
}