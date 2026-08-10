package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C2C4C),
    onPrimaryContainer = Color(0xFF8EB2FF),
    secondary = SecondaryIndigo,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = DarkTextMain,
    surface = DarkSurface,
    onSurface = DarkTextMain,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = DarkTextSub,
    error = PriorityRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F0FF),
    onPrimaryContainer = Color(0xFF0055B3),
    secondary = SecondaryIndigo,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightTextMain,
    surface = LightSurface,
    onSurface = LightTextMain,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = LightTextSub,
    error = PriorityRed,
    onError = Color.White
)

@Composable
fun VhxhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = VhxhTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

