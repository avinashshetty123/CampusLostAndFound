package com.example.lostandfoundfrontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Core Palette ──────────────────────────────────────────────────────────────
val Charcoal      = Color(0xFF1A1A2E)   // deep navy-charcoal
val Slate         = Color(0xFF2D2D44)   // mid charcoal
val SlateLight    = Color(0xFF3D3D5C)   // lighter slate for gradients
val IvoryWhite    = Color(0xFFFAFAF8)   // warm off-white background
val PaperWhite    = Color(0xFFFFFFFF)
val SurfaceGray   = Color(0xFFF4F4F2)   // card surface
val StrokeGray    = Color(0xFFE2E2DE)   // borders
val TextPrimary   = Color(0xFF1A1A2E)   // same as Charcoal
val TextSecond    = Color(0xFF6B6B80)   // muted purple-gray
val TextHint      = Color(0xFFAAAAAF)
val LostRed       = Color(0xFFD64045)
val LostRedBg     = Color(0xFFFFF0F0)
val FoundGreen    = Color(0xFF2D7A4F)
val FoundGreenBg  = Color(0xFFEDF7F1)
val AccentGold    = Color(0xFFC9A84C)   // subtle gold accent

// Legacy aliases kept for any remaining references
val Black         = Charcoal
val OffBlack      = Slate
val DarkGray      = SlateLight
val MidGray       = TextSecond
val LightGray     = TextHint
val DividerGray   = StrokeGray
val OffWhite      = SurfaceGray
val White         = PaperWhite
val MintGreen     = Charcoal
val MintGreenLight = SlateLight
val MintGreenDark  = Charcoal
val DarkHeader    = Charcoal
val SoftGray      = IvoryWhite
val BorderGray    = StrokeGray
val ErrorRed      = LostRed
val CardWhite     = PaperWhite
val TextSecondary = TextSecond
val AccentYellow  = AccentGold
val SurfaceLight  = SurfaceGray

private val AppColorScheme = lightColorScheme(
    primary            = Charcoal,
    onPrimary          = PaperWhite,
    primaryContainer   = SurfaceGray,
    onPrimaryContainer = Charcoal,
    secondary          = Slate,
    onSecondary        = PaperWhite,
    background         = IvoryWhite,
    onBackground       = Charcoal,
    surface            = PaperWhite,
    onSurface          = Charcoal,
    surfaceVariant     = SurfaceGray,
    onSurfaceVariant   = TextSecond,
    error              = LostRed,
    onError            = PaperWhite,
    outline            = StrokeGray,
    outlineVariant     = StrokeGray
)

@Composable
fun LostAndFoundTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, content = content)
}
