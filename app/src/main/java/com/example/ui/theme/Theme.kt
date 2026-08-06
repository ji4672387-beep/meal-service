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

private val DarkColorScheme =
  darkColorScheme(
    primary = MealPrimaryDark,
    background = MealBackgroundDark,
    surface = MealSurfaceDark,
    onBackground = MealOnBackgroundDark,
    onSurface = MealOnBackgroundDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MealPrimary,
    primaryContainer = MealPrimaryContainer,
    onPrimary = MealOnPrimary,
    onPrimaryContainer = MealOnPrimaryContainer,
    secondary = MealSecondary,
    secondaryContainer = MealSecondaryContainer,
    tertiary = MealTertiary,
    background = MealBackground,
    surface = MealSurface,
    surfaceVariant = MealSurfaceVariant,
    onBackground = MealOnBackground,
    onSurface = MealOnSurface,
    onSurfaceVariant = MealOnSurfaceVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
