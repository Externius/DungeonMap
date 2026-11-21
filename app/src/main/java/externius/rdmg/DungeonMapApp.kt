package externius.rdmg

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import externius.rdmg.helpers.ThemeHelper

@HiltAndroidApp
class DungeonMapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applyTheme(this)
    }
}
