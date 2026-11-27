package externius.rdmg.views

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.models.DungeonTile
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DungeonMapViewTest {

    @Test
    fun testLoadDungeonRecalculatesTileCoordinatesAndDimensions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dungeonMapView = DungeonMapView(context)
        dungeonMapView.dungeonWidth = 1000
        dungeonMapView.dungeonHeight = 1000

        // Create a 10x10 playable grid (12x12 array with boundaries) with old/stale coordinates
        val gridTileCount = 10
        val arraySize = gridTileCount + 2
        val tiles = Array(arraySize) { i ->
            Array(arraySize) { j ->
                DungeonTile(i, j, x = 0, y = 0, width = 50, height = 50)
            }
        }

        dungeonMapView.loadDungeon(
            dungeonTiles = tiles,
            roomDescription = emptyList(),
            trapDescription = emptyList(),
            hasCorridor = true,
            roamingMonsterDescription = emptyList()
        )

        val expectedImgSize = 1000 / gridTileCount // 100
        assertEquals(gridTileCount, dungeonMapView.dungeonSize)

        for (i in 1 until arraySize - 1) {
            for (j in 1 until arraySize - 1) {
                val tile = tiles[i][j]
                val expectedX = (j - 1) * expectedImgSize
                val expectedY = (i - 1) * expectedImgSize
                assertEquals("Tile ($i, $j) x mismatch", expectedX, tile.x)
                assertEquals("Tile ($i, $j) y mismatch", expectedY, tile.y)
                assertEquals("Tile ($i, $j) width mismatch", expectedImgSize, tile.width)
                assertEquals("Tile ($i, $j) height mismatch", expectedImgSize, tile.height)
            }
        }
    }

    @Test
    fun testOnSizeChangedRecalculatesTileCoordinatesAndDimensions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dungeonMapView = DungeonMapView(context)

        // Initial dungeon size before layout change
        val gridTileCount = 10
        val arraySize = gridTileCount + 2
        val tiles = Array(arraySize) { i ->
            Array(arraySize) { j ->
                DungeonTile(i, j, x = 0, y = 0, width = 50, height = 50)
            }
        }

        dungeonMapView.loadDungeon(
            dungeonTiles = tiles,
            roomDescription = emptyList(),
            trapDescription = emptyList(),
            hasCorridor = true,
            roamingMonsterDescription = emptyList()
        )

        // Trigger layout pass with new dimensions (1200x1200)
        dungeonMapView.layout(0, 0, 1200, 1200)

        val newExpectedImgSize = 1200 / gridTileCount // 120
        assertEquals(1200, dungeonMapView.dungeonWidth)
        assertEquals(1200, dungeonMapView.dungeonHeight)

        for (i in 1 until arraySize - 1) {
            for (j in 1 until arraySize - 1) {
                val tile = tiles[i][j]
                val expectedX = (j - 1) * newExpectedImgSize
                val expectedY = (i - 1) * newExpectedImgSize
                assertEquals("Tile ($i, $j) x mismatch after resize", expectedX, tile.x)
                assertEquals("Tile ($i, $j) y mismatch after resize", expectedY, tile.y)
                assertEquals("Tile ($i, $j) width mismatch after resize", newExpectedImgSize, tile.width)
                assertEquals("Tile ($i, $j) height mismatch after resize", newExpectedImgSize, tile.height)
            }
        }
    }
}
