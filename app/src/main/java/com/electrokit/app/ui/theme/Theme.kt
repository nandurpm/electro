package com.electrokit.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Deep navy brand surface for app bars and branded loading states. */
val BrandNavy = Color(0xFF12243A)
/** High-contrast toolbar foreground. */
val TopBarText = Color(0xFFF5F8FC)
/** Theme-aware emphasis for headings and active controls. */
val Heading: Color
    @Composable get() = MaterialTheme.colorScheme.primary
/** Theme-aware separator tone. */
val Divider: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

/** Midnight palette with cool teal emphasis and warm amber highlights. */
private val Dark =
    darkColorScheme(
        primary = Color(0xFF64DFC9),
        onPrimary = Color(0xFF003A33),
        primaryContainer = Color(0xFF174A49),
        onPrimaryContainer = Color(0xFFC7F6EA),
        secondary = Color(0xFFB7D0E3),
        onSecondary = Color(0xFF173046),
        secondaryContainer = Color(0xFF263A4E),
        onSecondaryContainer = Color(0xFFDCEAF4),
        tertiary = Color(0xFFFFD28A),
        onTertiary = Color(0xFF443000),
        tertiaryContainer = Color(0xFF5A431E),
        onTertiaryContainer = Color(0xFFFFE8BD),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF742922),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF90A7BA),
        outlineVariant = Color(0xFF35475B),
        inverseSurface = Color(0xFFE6EEF6),
        inverseOnSurface = Color(0xFF1C2B3D),
        inversePrimary = Color(0xFF00695C),
        surfaceTint = Color(0xFF64DFC9),
        surfaceDim = Color(0xFF0A111D),
        surfaceBright = Color(0xFF34485F),
        surfaceContainerLowest = Color(0xFF0B1422),
        surfaceContainerLow = Color(0xFF111D2E),
        surfaceContainer = Color(0xFF1A293D),
        surfaceContainerHigh = Color(0xFF202F43),
        surfaceContainerHighest = Color(0xFF29394E),
        background = Color(0xFF0C1422),
        surface = Color(0xFF121E2F),
        surfaceVariant = Color(0xFF233449),
        onBackground = Color(0xFFEDF3FA),
        onSurface = Color(0xFFEDF3FA),
        onSurfaceVariant = Color(0xFFB7C5D3),
    )

/** Soft neutral palette with the same teal-and-amber brand accents. */
private val Light =
    lightColorScheme(
        primary = Color(0xFF00695C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC5F1E8),
        onPrimaryContainer = Color(0xFF00483F),
        secondary = Color(0xFF385F78),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDCEBF4),
        onSecondaryContainer = Color(0xFF1B3E52),
        tertiary = Color(0xFF815300),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE8BD),
        onTertiaryContainer = Color(0xFF493100),
        error = Color(0xFFA32622),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF62788B),
        outlineVariant = Color(0xFFCAD5DF),
        inverseSurface = Color(0xFF26394C),
        inverseOnSurface = Color(0xFFECF3F8),
        inversePrimary = Color(0xFF64DFC9),
        surfaceTint = Color(0xFF00695C),
        surfaceDim = Color(0xFFD8E1E9),
        surfaceBright = Color(0xFFF8FAFC),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F8FB),
        surfaceContainer = Color(0xFFE9EFF5),
        surfaceContainerHigh = Color(0xFFE1EAF2),
        surfaceContainerHighest = Color(0xFFD7E2EC),
        background = Color(0xFFF5F7FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE8EEF4),
        onBackground = Color(0xFF182638),
        onSurface = Color(0xFF182638),
        onSurfaceVariant = Color(0xFF485C70),
    )

/** Applies the complete light or dark palette to all app surfaces and controls.
 *
 * @param light True selects the light palette; false selects the midnight palette.
 * @param content App subtree rendered with the selected palette.
 * @return Unit after providing the Material theme to descendants.
 */
@Composable
fun ElectroTheme(light: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (light) Light else Dark, content = content)
}

/** Neutral metal shared by diagrams; identification wire colours remain separate. */
val DrawingMetal = Color(0xFFD7E3E4)
