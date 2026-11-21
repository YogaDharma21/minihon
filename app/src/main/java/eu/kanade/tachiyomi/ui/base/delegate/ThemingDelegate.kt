package eu.kanade.tachiyomi.ui.base.delegate

import android.app.Activity
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.tachiyomi.R

interface ThemingDelegate {
    fun applyAppTheme(activity: Activity)

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun getThemeResIds(appTheme: AppTheme = AppTheme.DEFAULT, isAmoled: Boolean = false): List<Int> {
            return listOf(R.style.Theme_Tachiyomi)
        }
    }
}

class ThemingDelegateImpl : ThemingDelegate {
    override fun applyAppTheme(activity: Activity) {
        activity.setTheme(R.style.Theme_Tachiyomi)
    }
}
