package externius.rdmg

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import externius.rdmg.helpers.ThemeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class DungeonMapApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applyTheme(this, applicationScope)
    }
}
