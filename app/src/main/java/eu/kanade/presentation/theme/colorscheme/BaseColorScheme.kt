package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.ColorScheme

internal abstract class BaseColorScheme {

    abstract val darkScheme: ColorScheme
    abstract val lightScheme: ColorScheme

    fun getColorScheme(
        isDark: Boolean = true,
        isAmoled: Boolean = false,
        overrideDarkSurfaceContainers: Boolean = false,
    ): ColorScheme {
        return darkScheme
    }
}
