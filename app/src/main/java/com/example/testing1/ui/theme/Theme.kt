package com.example.testing1.ui.theme

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
    primary = DarkCoffee,
    onPrimary = Color.White,
    secondary = GoldenBadge,
    onSecondary = DeepCocoaDark,
    tertiary = FlameAmberLight,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF35241C),
    onSurfaceVariant = Color(0xFFD4C5B9)
)

private val LightColorScheme = lightColorScheme(
    primary = FlameAmber,
    onPrimary = Color.White,
    secondary = GoldenBadge,
    onSecondary = DeepCocoaDark,
    tertiary = DeepCocoa,
    background = WarmCream,
    surface = Color.White,
    onBackground = DeepCocoa,
    onSurface = DeepCocoa,
    surfaceVariant = WarmCreamCard,
    onSurfaceVariant = Color(0xFF7A6256)
)

@Composable
fun Testing1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
