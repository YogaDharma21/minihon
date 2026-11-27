package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

internal object MinimalistDarkColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFF2F1EC),
        onPrimary = Color(0xFF0A0A0B),
        primaryContainer = Color(0xFF1F2025),
        onPrimaryContainer = Color(0xFFF4F4F2),
        inversePrimary = Color(0xFF0A0A0B),
        secondary = Color(0xFFA6B0BD),
        onSecondary = Color(0xFF0A0A0B),
        secondaryContainer = Color(0xFF1F2025),
        onSecondaryContainer = Color(0xFFF4F4F2),
        tertiary = Color(0xFFA9BFA9),
        onTertiary = Color(0xFF0A0A0B),
        tertiaryContainer = Color(0xFF1B231B),
        onTertiaryContainer = Color(0xFFA9BFA9),
        background = Color(0xFF08090B),
        onBackground = Color(0xFFF4F4F2),
        surface = Color(0xFF08090B),
        onSurface = Color(0xFFF4F4F2),
        surfaceVariant = Color(0xFF17181C),
        onSurfaceVariant = Color(0xFF8A8C92),
        surfaceTint = Color.Transparent,
        inverseSurface = Color(0xFF1F2025),
        inverseOnSurface = Color(0xFFF4F4F2),
        error = Color(0xFFC99A90),
        onError = Color(0xFF0A0A0B),
        errorContainer = Color(0xFF2E1715),
        onErrorContainer = Color(0xFFC99A90),
        outline = Color(0xFF2D2E33),
        outlineVariant = Color(0xFF1F2025),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF17181C),
        surfaceDim = Color(0xFF08090B),
        surfaceContainerLowest = Color(0xFF08090B),
        surfaceContainerLow = Color(0xFF0E0F11),
        surfaceContainer = Color(0xFF121316),
        surfaceContainerHigh = Color(0xFF17181C),
        surfaceContainerHighest = Color(0xFF1F2025),
    )

    override val lightScheme = darkScheme
}
