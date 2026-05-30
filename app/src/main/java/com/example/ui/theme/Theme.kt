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

private val DarkColorScheme =
  darkColorScheme(
    primary = LabTealDark,
    secondary = LabSlateDark,
    tertiary = LabBlueDark,
    background = LabBackgroundDark,
    surface = LabSurfaceDark,
    onPrimary = Color(0xFF003731),
    onSecondary = Color(0xFF1C3532),
    onBackground = Color(0xFFE0E3E1),
    onSurface = Color(0xFFE0E3E1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LabTealLight,
    secondary = LabSlateLight,
    tertiary = LabBlueLight,
    background = LabBackgroundLight,
    surface = LabSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
