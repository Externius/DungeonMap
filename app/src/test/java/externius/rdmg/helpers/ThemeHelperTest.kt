package externius.rdmg.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ThemeHelperTest {

    @Test
    fun testSetAndGetThemeMode() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        ThemeHelper.setThemeMode(context, ThemeHelper.MODE_LIGHT)
        assertEquals(ThemeHelper.MODE_LIGHT, ThemeHelper.getSavedThemeMode(context))

        ThemeHelper.setThemeMode(context, ThemeHelper.MODE_NIGHT)
        assertEquals(ThemeHelper.MODE_NIGHT, ThemeHelper.getSavedThemeMode(context))

        val flowMode = ThemeHelper.getSavedThemeModeFlow(context).first()
        assertEquals(ThemeHelper.MODE_NIGHT, flowMode)
    }

    @Test
    fun testApplyTheme() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ThemeHelper.setThemeMode(context, ThemeHelper.MODE_NIGHT)

        ThemeHelper.applyTheme(context, backgroundScope)
        testScheduler.advanceUntilIdle()
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }
}
