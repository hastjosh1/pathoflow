package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// PathoFlow palette — "minimal, but legible outdoors"
// White cards on a soft neutral canvas, near-black text for
// sunlight readability, teal reserved for accents and actions.
// ============================================================

// ---------- Light scheme ----------
val md_light_primary = Color(0xFF006A5F)
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFD5F2EA)
val md_light_onPrimaryContainer = Color(0xFF00332C)
val md_light_secondary = Color(0xFF526460)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFE3E8E6)
val md_light_onSecondaryContainer = Color(0xFF20302D)
val md_light_tertiary = Color(0xFF4E6470)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFE1E9ED)
val md_light_onTertiaryContainer = Color(0xFF1C3540)
val md_light_error = Color(0xFFB3261E)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFF9DEDC)
val md_light_onErrorContainer = Color(0xFF410E0B)
// Canvas: soft neutral gray so white cards read as clear surfaces
val md_light_background = Color(0xFFF3F5F4)
val md_light_onBackground = Color(0xFF161918)
val md_light_surface = Color(0xFFF3F5F4)
val md_light_onSurface = Color(0xFF161918)
val md_light_surfaceVariant = Color(0xFFE6E9E8)
// Darker than typical SaaS gray — field/outdoor legibility
val md_light_onSurfaceVariant = Color(0xFF505755)
val md_light_outline = Color(0xFF6F7774)
val md_light_outlineVariant = Color(0xFFDDE1DF)
val md_light_inverseSurface = Color(0xFF2B302E)
val md_light_inverseOnSurface = Color(0xFFEFF2F0)
val md_light_inversePrimary = Color(0xFF7ED9C9)
// Cards are pure white on the gray canvas
val md_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_light_surfaceContainerLow = Color(0xFFFFFFFF)
val md_light_surfaceContainer = Color(0xFFFBFCFB)
val md_light_surfaceContainerHigh = Color(0xFFECEFEE)
val md_light_surfaceContainerHighest = Color(0xFFE4E8E6)

// ---------- Dark scheme ----------
val md_dark_primary = Color(0xFF7ED9C9)
val md_dark_onPrimary = Color(0xFF00382F)
val md_dark_primaryContainer = Color(0xFF005046)
val md_dark_onPrimaryContainer = Color(0xFFA8F0E2)
val md_dark_secondary = Color(0xFFB4C6C1)
val md_dark_onSecondary = Color(0xFF1F312D)
val md_dark_secondaryContainer = Color(0xFF364845)
val md_dark_onSecondaryContainer = Color(0xFFD0E2DD)
val md_dark_tertiary = Color(0xFFB6CDDA)
val md_dark_onTertiary = Color(0xFF213640)
val md_dark_tertiaryContainer = Color(0xFF374D57)
val md_dark_onTertiaryContainer = Color(0xFFD2E5F0)
val md_dark_error = Color(0xFFF2B8B5)
val md_dark_onError = Color(0xFF601410)
val md_dark_errorContainer = Color(0xFF8C1D18)
val md_dark_onErrorContainer = Color(0xFFF9DEDC)
val md_dark_background = Color(0xFF101413)
val md_dark_onBackground = Color(0xFFE4E7E5)
val md_dark_surface = Color(0xFF101413)
val md_dark_onSurface = Color(0xFFE4E7E5)
val md_dark_surfaceVariant = Color(0xFF414846)
val md_dark_onSurfaceVariant = Color(0xFFB8C0BD)
val md_dark_outline = Color(0xFF828B88)
val md_dark_outlineVariant = Color(0xFF3F4644)
val md_dark_inverseSurface = Color(0xFFE4E7E5)
val md_dark_inverseOnSurface = Color(0xFF2B302E)
val md_dark_inversePrimary = Color(0xFF006A5F)
val md_dark_surfaceContainerLowest = Color(0xFF0B0F0E)
val md_dark_surfaceContainerLow = Color(0xFF1A1E1D)
val md_dark_surfaceContainer = Color(0xFF1E2321)
val md_dark_surfaceContainerHigh = Color(0xFF272C2A)
val md_dark_surfaceContainerHighest = Color(0xFF313634)

// ---------- Status colors (used directly in screens) ----------
val StatusSuccess = Color(0xFF2E7D32)
val StatusWarning = Color(0xFFEF6C00)
val StatusDanger = Color(0xFFC62828)
val StatusInfo = Color(0xFF1565C0)

// ---------- Legacy aliases (kept so existing screen code compiles) ----------
val LabTealLight = md_light_primary
val LabSlateLight = md_light_secondary
val LabBlueLight = md_light_tertiary
val LabBackgroundLight = md_light_background
val LabSurfaceLight = md_light_surface
val LabTealDark = md_dark_primary
val LabSlateDark = md_dark_secondary
val LabBlueDark = md_dark_tertiary
val LabBackgroundDark = md_dark_background
val LabSurfaceDark = md_dark_surface
