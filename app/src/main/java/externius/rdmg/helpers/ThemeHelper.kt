package externius.rdmg.helpers

import android.content.Context
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import externius.rdmg.R

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_NIGHT_MODE = "night_mode"

    const val MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    const val MODE_NIGHT = AppCompatDelegate.MODE_NIGHT_YES
    const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO

    fun applyTheme(context: Context) {
        val mode = getSavedThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getSavedThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_NIGHT_MODE, MODE_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_NIGHT_MODE, mode) }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

fun Button.applyPrimaryStyle() {
    setBackgroundColor(context.getColor(R.color.primaryAccent))
    setTextColor(context.getColor(R.color.buttonTextColor))
}
