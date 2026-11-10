package externius.rdmg.viewmodels

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(application)
    }

    @Test
    fun testDungeonSummariesFlow() = runTest {
        backgroundScope.launch {
            viewModel.dungeonSummaries.collect {}
        }
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(application).dungeonDao()
            dao.deleteAll()
            dao.insert(createTestEntity("Dungeon MVM 1", "{\"tiles\":[201]}"))
        }

        val summaries = viewModel.dungeonSummaries.first { it.isNotEmpty() }
        assertEquals(1, summaries.size)
        assertEquals("Dungeon MVM 1", summaries[0].dungeonName)
    }

    @Test
    fun testGetDungeonById() = runTest {
        val id: Long
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(application).dungeonDao()
            dao.deleteAll()
            id = dao.insert(createTestEntity("Dungeon MVM 2", "{\"tiles\":[202]}"))
        }

        val retrieved = viewModel.getDungeonById(id)
        assertNotNull(retrieved)
        assertEquals("Dungeon MVM 2", retrieved?.dungeonName)
    }

    @Test
    fun testDeleteDungeon() = runTest {
        val id: Long
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(application).dungeonDao()
            dao.deleteAll()
            id = dao.insert(createTestEntity("Dungeon to delete", "{\"tiles\":[203]}"))
        }

        assertNotNull(viewModel.getDungeonById(id))
        viewModel.deleteDungeon(id)

        var retrieved: DungeonEntity? = null
        var attempts = 0
        while (attempts < 20) {
            retrieved = viewModel.getDungeonById(id)
            if (retrieved == null) break
            kotlinx.coroutines.delay(50.milliseconds)
            attempts++
        }
        assertNull(retrieved)
    }

    @Test
    fun testDeleteAllDungeons() = runTest {
        backgroundScope.launch {
            viewModel.dungeonSummaries.collect {}
        }
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(application).dungeonDao()
            dao.deleteAll()
            dao.insert(createTestEntity("Dungeon A", "{\"tiles\":[204]}"))
            dao.insert(createTestEntity("Dungeon B", "{\"tiles\":[205]}"))
        }

        viewModel.deleteAllDungeons()

        val summaries = viewModel.dungeonSummaries.first { it.isEmpty() }
        assertTrue(summaries.isEmpty())
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
