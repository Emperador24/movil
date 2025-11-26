package com.example.liftium.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.liftium.ui.theme.LiftiumOrangeLighter

// Brand Colors
val LiftiumOrange = Color(0xFFFF5722)
val LiftiumOrangeDark = Color(0xFFE64A19)
val LiftiumOrangeLight = Color(0xFFFF8A65)
val LiftiumOrangeLighter = Color(0xFFE78E73)

// Dark Theme Colors
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2D2D2D)
val DarkSurfaceVariant = Color(0xFF3A3A3A)
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0)

// Light Theme Colors
val LightBackground = Color(0x80FAFAFA)
val LightSurface = Color(0xBFFAFAFA)
val LightSurfaceVariant = Color(0xFFFAFAFA)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnSurfaceVariant = Color(0xFF666666)

val LiftiumLightColorScheme = lightColorScheme(
    primary = LiftiumOrange,
    onPrimary = Color.White,
    primaryContainer = LiftiumOrangeLight,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFD53529),
    onSecondary = Color.White,
    tertiary = Color(0xFF03DAC6),
    onTertiary = LiftiumOrangeLighter,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFCACACA),
    error = Color(0xFFB00020),
    onError = Color.White,
)

val LiftiumDarkColorScheme = darkColorScheme(
    primary = LiftiumOrange,
    onPrimary = Color.White,
    primaryContainer = LiftiumOrangeDark,
    onPrimaryContainer = Color.Gray,
    secondary = Color(0xFFD53529),
    onSecondary = Color.White,
    tertiary = Color(0xFF03DAC6),
    onTertiary = LiftiumOrangeLighter,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF4A4A4A),
    error = Color(0xFFCF6679),
    onError = Color.Black,
)
