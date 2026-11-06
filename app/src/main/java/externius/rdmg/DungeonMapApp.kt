package externius.rdmg

import android.app.Application
import externius.rdmg.helpers.ThemeHelper

class DungeonMapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applyTheme(this)
    }
}
