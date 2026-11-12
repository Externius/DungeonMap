package externius.rdmg.viewmodels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertArrayEquals
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
        viewModel = DungeonViewModel(application, SavedStateHandle())
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

    @Test
    fun testRetainDungeonDataInViewModel() {
        val tiles = arrayOf(arrayOf(externius.rdmg.models.DungeonTile(0, 0)))
        val roomDesc = mutableListOf(
            externius.rdmg.models.RoomDescription(
                "Room 1",
                "10 Gp",
                "Giant Wolf Spider (CR: 1/4) 50 XP",
                "Door N",
            )
        )
        val trapDesc = mutableListOf(externius.rdmg.models.TrapDescription("Trap 1", "Pit"))
        val monsterDesc =
            mutableListOf(externius.rdmg.models.RoamingMonsterDescription("Monster 1", "Orc"))

        viewModel.loadedDungeon = tiles
        viewModel.loadedRoomDescription = roomDesc
        viewModel.loadedTrapDescription = trapDesc
        viewModel.loadedRoamingMonsterDescription = monsterDesc

        assertArrayEquals(tiles, viewModel.loadedDungeon)
        assertEquals(roomDesc, viewModel.loadedRoomDescription)
        assertEquals(trapDesc, viewModel.loadedTrapDescription)
        assertEquals(monsterDesc, viewModel.loadedRoamingMonsterDescription)
    }

    @Test
    fun testRetainEditPopupStateInSavedStateHandle() {
        val roomPopupState = externius.rdmg.models.EditPopupState(
            index = 1,
            texture = externius.rdmg.models.Textures.ROOM,
            monsterText = "Updated Monster",
            treasureText = "Updated Treasure",
        )
        viewModel.editPopupState = roomPopupState

        assertEquals(roomPopupState, viewModel.editPopupState)

        // Create a new ViewModel with the same SavedStateHandle (simulating recreation/rotation)
        var recreatedViewModel = DungeonViewModel(application, viewModel.savedStateHandle)
        assertEquals(roomPopupState, recreatedViewModel.editPopupState)

        val trapPopupState = externius.rdmg.models.EditPopupState(
            index = 0,
            texture = externius.rdmg.models.Textures.TRAP,
            descriptionText = "Updated Trap Description",
        )
        viewModel.editPopupState = trapPopupState
        recreatedViewModel = DungeonViewModel(application, viewModel.savedStateHandle)
        assertEquals(trapPopupState, recreatedViewModel.editPopupState)
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
