package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CyberpunkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonGreen,
    tertiary = NeonCyan,
    background = MatrixDark,
    surface = GlassBackground,
    onPrimary = MatrixDark,
    onSecondary = MatrixDark,
    onTertiary = MatrixDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        typography = Typography,
        content = content
    )
}
