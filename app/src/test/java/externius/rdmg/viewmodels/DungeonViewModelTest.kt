package externius.rdmg.viewmodels

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DungeonViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: DungeonViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = DungeonViewModel(application)
    }

    @Test
    fun testInsertAndGetDungeon() = runTest {
        withContext(Dispatchers.IO) {
            AppDatabase.getInstance(application).clearAllTables()
        }
        val entity = createTestEntity("Test Dungeon VM", "{\"tiles\":[101]}")
        val id = viewModel.insertDungeon(entity)
        assertTrue("Inserted Id should be positive", id > 0)

        val retrieved = viewModel.getDungeonById(id)
        assertNotNull("Retrieved entity should not be null", retrieved)
        assertEquals("Test Dungeon VM", retrieved?.dungeonName)
    }

    @Test
    fun testUpdateDungeon() = runTest {
        withContext(Dispatchers.IO) {
            AppDatabase.getInstance(application).clearAllTables()
        }
        val entity = createTestEntity("VM Update Test", "{\"tiles\":[102]}")
        val id = viewModel.insertDungeon(entity)

        val initial = viewModel.getDungeonById(id)
        assertNotNull(initial)

        val updated = initial!!.copy(dungeonDifficulty = "Hard")
        val count = viewModel.updateDungeon(updated)
        assertEquals(1, count)

        val reFetched = viewModel.getDungeonById(id)
        assertEquals("Hard", reFetched?.dungeonDifficulty)
    }

    private fun createTestEntity(name: String, loadedDungeon: String): DungeonEntity {
        return DungeonEntity(
            dungeonName = name,
            loadedDungeon = loadedDungeon,
            loadedRoomDescription = "[]",
            loadedTrapDescription = "[]",
            loadedRoamingMonsters = "[]",
            dungeonDifficulty = "Medium",
            partyLevel = "1",
            partySize = "4",
            treasureValue = "1.0",
            itemsRarity = "3",
            dungeonSize = "15",
            roomDensity = "20",
            roomSize = "20",
            traps = "Few",
            corridors = "YES",
            monsterType = "any",
            deadEnds = "NO",
            roamingMonsters = "Few",
            dungeonCreated = LocalDate.now().toString()
        )
    }
}
