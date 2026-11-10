package externius.rdmg.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.After
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
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dungeonDao: DungeonDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dungeonDao = database.dungeonDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndGetById() {
        val entity = createTestEntity("Dungeon 1", "{\"tiles\":[]}")
        val id = dungeonDao.insert(entity)
        assertTrue("Inserted Id should be positive", id > 0)

        val retrieved = dungeonDao.getDungeonById(id)
        assertNotNull("Retrieved entity should not be null", retrieved)
        assertEquals("Dungeon 1", retrieved?.dungeonName)
        assertEquals("{\"tiles\":[]}", retrieved?.loadedDungeon)
        assertEquals("Medium", retrieved?.dungeonDifficulty)
        assertEquals("4", retrieved?.partySize)
    }

    @Test
    fun testGetDungeonSummaries() {
        val entity1 = createTestEntity("Dungeon_Alpha", "{\"tiles\":[1]}")
        val entity2 = createTestEntity("Dungeon_Beta", "{\"tiles\":[2]}")

        val id1 = dungeonDao.insert(entity1)
        val id2 = dungeonDao.insert(entity2)

        val summaries = dungeonDao.getDungeonSummaries()
        assertEquals(2, summaries.size)

        val ids = summaries.map { it.id }
        assertTrue(ids.contains(id1))
        assertTrue(ids.contains(id2))

        val names = summaries.map { it.dungeonName }
        assertTrue(names.contains("Dungeon_Alpha"))
        assertTrue(names.contains("Dungeon_Beta"))
    }

    @Test
    fun testGetDungeonSummariesFlow() = runTest {
        dungeonDao.insert(createTestEntity("Dungeon_Flow", "{\"tiles\":[100]}"))

        val summaries = dungeonDao.getDungeonSummariesFlow().first()
        assertEquals(1, summaries.size)
        assertEquals("Dungeon_Flow", summaries[0].dungeonName)
    }

    @Test
    fun testUpdateDungeon() {
        val entity = createTestEntity("Dungeon_Initial", "{\"tiles\":[]}")
        val id = dungeonDao.insert(entity)

        val initial = dungeonDao.getDungeonById(id)
        assertNotNull(initial)

        val updatedEntity = initial!!.copy(
            loadedRoomDescription = "[{\"name\":\"Room 1\"}]",
            loadedTrapDescription = "[{\"name\":\"Trap 1\"}]"
        )
        val updatedRows = dungeonDao.update(updatedEntity)
        assertEquals(1, updatedRows)

        val reFetched = dungeonDao.getDungeonById(id)
        assertEquals("[{\"name\":\"Room 1\"}]", reFetched?.loadedRoomDescription)
        assertEquals("[{\"name\":\"Trap 1\"}]", reFetched?.loadedTrapDescription)
    }

    @Test
    fun testDeleteById() {
        val id = dungeonDao.insert(createTestEntity("Dungeon_To_Delete", "{\"tiles\":[]}"))
        assertNotNull(dungeonDao.getDungeonById(id))

        val deletedRows = dungeonDao.deleteById(id)
        assertEquals(1, deletedRows)
        assertThat(dungeonDao.getDungeonById(id), nullValue())
    }

    @Test
    fun testDeleteAll() {
        dungeonDao.insert(createTestEntity("Test1", "{\"t\":1}"))
        dungeonDao.insert(createTestEntity("Test2", "{\"t\":2}"))
        assertEquals(2, dungeonDao.getDungeonSummaries().size)

        dungeonDao.deleteAll()
        assertEquals(0, dungeonDao.getDungeonSummaries().size)
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
