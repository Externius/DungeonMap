package externius.rdmg

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.helpers.Export
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExportTest {

    @Test
    fun generateHTML_withNullBitmap_returnsHtmlWithoutCrash() {
        val html = Export.generateHTML(null, null, null, null)
        assertNotNull(html)
        assertTrue(html.contains("<html>"))
        assertTrue(html.contains("</html>"))
        assertFalse(html.contains("<img src="))
    }

    @Test
    fun generateHTML_withValidBitmap_includesBase64Image() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val html = Export.generateHTML(bitmap, null, null, null)
        assertNotNull(html)
        assertTrue(html.contains("data:image/png;base64,"))
        bitmap.recycle()
    }

    @Test
    fun generateHTML_withRecycledBitmap_returnsHtmlGracefully() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.recycle()
        val html = Export.generateHTML(bitmap, null, null, null)
        assertNotNull(html)
        assertTrue(html.contains("<html>"))
        assertFalse(html.contains("<img src="))
    }

    @Test
    fun generateHTML_withLargeBitmap_downsamplesWithoutError() {
        val largeBitmap = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        val html = Export.generateHTML(largeBitmap, null, null, null)
        assertNotNull(html)
        assertTrue(html.contains("data:image/png;base64,"))
        largeBitmap.recycle()
    }

    @Test
    fun generateHTML_withDescriptions_includesDescriptions() {
        val rooms = listOf(RoomDescription("Room 1", "Treasure 100gp", "Orc", "North Door"))
        val traps = listOf(TrapDescription("Pit Trap", "DC 15 Dex save"))
        val monsters = listOf(RoamingMonsterDescription("Goblin Patrol", "3 Goblins"))

        val html = Export.generateHTML(null, rooms, traps, monsters)
        assertTrue(html.contains("Room 1"))
        assertTrue(html.contains("Pit Trap"))
        assertTrue(html.contains("Goblin Patrol"))
    }
}
