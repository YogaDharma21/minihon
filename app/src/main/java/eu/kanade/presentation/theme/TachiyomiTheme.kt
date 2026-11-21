package eu.kanade.presentation.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.presentation.theme.colorscheme.MinimalistDarkColorScheme

@Composable
fun TachiyomiTheme(
    appTheme: AppTheme? = null,
    amoled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    BaseTachiyomiTheme(
        content = content,
    )
}

@Composable
fun TachiyomiPreviewTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit,
) = BaseTachiyomiTheme(content = content)

@Composable
private fun BaseTachiyomiTheme(
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = MinimalistDarkColorScheme.getColorScheme(),
        content = content,
    )
}
