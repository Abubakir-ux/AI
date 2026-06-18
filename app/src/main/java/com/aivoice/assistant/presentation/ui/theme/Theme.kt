package com.aivoice.assistant.presentation.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Asosiy ranglar
val PrimaryPurple = Color(0xFF7C4DFF)
val PrimaryPurpleLight = Color(0xFFB47CFF)
val SecondaryBlue = Color(0xFF00BCD4)
val AccentGreen = Color(0xFF00E676)
val AccentOrange = Color(0xFFFF6D00)
val ErrorRed = Color(0xFFFF1744)

// Dark theme ranglar
val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF12121A)
val DarkCard = Color(0xFF1A1A28)
val DarkCardElevated = Color(0xFF22223A)
val DarkBorder = Color(0xFF2A2A45)

// Matn ranglar
val TextPrimary = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFF9090B0)
val TextMuted = Color(0xFF5A5A80)

// AI animatsiya ranglar
val GlowPurple = Color(0x667C4DFF)
val GlowBlue = Color(0x6600BCD4)
val GlowGreen = Color(0x6600E676)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF006064),
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGreen,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun AIVoiceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
