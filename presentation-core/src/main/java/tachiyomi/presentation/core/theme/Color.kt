package tachiyomi.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorScheme.active: Color
    @Composable
    get() {
        return if (isSystemInDarkTheme()) Color(255, 235, 59) else Color(255, 193, 7)
    }

val ColorScheme.emerald: Color
    get() = Color(0xFFA9BFA9)

val ColorScheme.crimson: Color
    get() = Color(0xFFC99A90)

val ColorScheme.amber: Color
    get() = Color(0xFFD2BD93)

val ColorScheme.steelBlue: Color
    get() = Color(0xFFA6B0BD)

val ColorScheme.subtle: Color
    get() = Color(0xFF5F6065)

val ColorScheme.borderHairline: Color
    get() = Color(0xFFFFFFFF).copy(alpha = 0.07f)
