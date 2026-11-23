package externius.rdmg.helpers

import android.content.Context
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import externius.rdmg.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_prefs",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "theme_prefs"))
    }
)

object ThemeHelper {
    private val KEY_NIGHT_MODE = intPreferencesKey("night_mode")

    const val MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    const val MODE_NIGHT = AppCompatDelegate.MODE_NIGHT_YES
    const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO

    fun getSavedThemeModeFlow(context: Context): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_NIGHT_MODE] ?: MODE_SYSTEM
            }
            .distinctUntilChanged()
    }

    suspend fun getSavedThemeMode(context: Context): Int {
        return getSavedThemeModeFlow(context).first()
    }

    suspend fun setThemeMode(context: Context, mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NIGHT_MODE] = mode
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applyTheme(context: Context, scope: CoroutineScope) {
        scope.launch {
            getSavedThemeModeFlow(context).collect { mode ->
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }
}

fun Button.applyPrimaryStyle() {
    setBackgroundColor(context.getColor(R.color.primaryAccent))
    setTextColor(context.getColor(R.color.buttonTextColor))
}
