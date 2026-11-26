package externius.rdmg.helpers

import android.util.Log
import externius.rdmg.activities.MainActivity
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.util.Scanner

class DungeonHelperTest {

    private lateinit var helper: DungeonHelper

    @get:Rule
    val name = TestName()

    @Before
    fun setup() {
        helper = DungeonHelper()
    }

    @Test
    fun testManhattan() {
        val result = helper.manhattan(3, 4)
        assertEquals(7, result)
    }

    @Test
    fun testGetRandomInt() {
        repeat(100) {
            val valBetween = helper.getRandomInt(2, 9)
            assertNotEquals("returns lower value than expected", 1, valBetween)
            assertNotEquals("returns max value", 9, valBetween)
        }
        val equalVal = helper.getRandomInt(5, 5)
        assertEquals(5, equalVal)
        val minGreaterThanMax = helper.getRandomInt(8, 3)
        assertEquals(8, minGreaterThanMax)
    }

    @Test
    fun testAddTrapDescription() {
        val dungeon = getTiles()
        val trapDescriptions = mutableListOf<TrapDescription>()
        helper.addTrapDescription(
            dungeon,
            2,
            2,
            trapDescriptions,
            dungeonDifficulty = 1,
            partyLevel = 1
        )
        assertEquals(1, trapDescriptions.size)
        assertEquals("1", dungeon[2][2].description)
    }

    @Test
    fun testAddTrapDescriptionMultiple() {
        val params = helper.createParams(1, 4, 1, 1.0, 1, "any", null, null)
        val dungeon = getTiles()
        val trapDescription = ArrayList<TrapDescription>()
        for (i in 0 until 3) {
            helper.addTrapDescription(
                dungeon,
                i + 1,
                i + 1,
                trapDescription,
                params.dungeonDifficulty,
                params.partyLevel
            )
        }
        assertEquals(3, trapDescription.size)
    }

    @Test
    fun testAddRoamingMonsterDescription() {
        val params = helper.createParams(1, 4, 1, 1.0, 1, "any", null, null)
        val dungeon = getTiles()
        val roamingDescriptions = mutableListOf<RoamingMonsterDescription>()
        helper.addRoamingMonsterDescription(dungeon, 3, 3, roamingDescriptions, params)
        assertEquals(1, roamingDescriptions.size)
        assertEquals("1", dungeon[3][3].description)
    }

    @Test
    fun testAddRoomDescription() {
        val params = helper.createParams(1, 4, 1, 1.0, 1, "any", null, null)
        val dungeon = getTiles()
        val roomDescription = ArrayList<RoomDescription>()
        val doors = ArrayList<DungeonTile>()
        doors.add(DungeonTile(3, 3, 50, 50, 50, 50))
        dungeon[3][3].texture = Textures.DOOR
        dungeon[3][4].texture = Textures.ROOM
        for (i in 0 until 3) {
            helper.addRoomDescription(dungeon, i + 1, i + 1, roomDescription, doors, params)
        }
        assertEquals(3, roomDescription.size)
    }

    @Test
    fun testAddNCRoomDescription() {
        val params = helper.createParams(1, 4, 1, 1.0, 1, "any", null, null)
        val dungeon = getTiles()
        val roomDescriptions = mutableListOf<RoomDescription>()
        helper.addNCRoomDescription(dungeon, 4, 4, roomDescriptions, "Door Info", params)
        assertEquals(1, roomDescriptions.size)
        assertEquals("1", dungeon[4][4].description)
        assertEquals("Door Info", roomDescriptions[0].doors)
    }

    @Test
    fun testParseMonsters_validJson_returnsFilteredList() {
        val json = readJSON("monsters.json") ?: ""
        val monsters = helper.parseMonsters(json, partyLevel = 1, monsterType = "any")
        assertNotNull(monsters)
        assertTrue("Parsed monsters list should not be empty", monsters.isNotEmpty())
    }

    @Test
    fun testParseMonsters_withCommaSeparatedTypes() {
        val json = readJSON("monsters.json") ?: ""
        val monsters = helper.parseMonsters(json, partyLevel = 1, monsterType = "humanoid, beast")
        assertNotNull(monsters)
    }

    @Test
    fun testParseMonsters_emptyJson_returnsEmptyList() {
        val monsters = helper.parseMonsters("", partyLevel = 1, monsterType = "any")
        assertTrue(monsters.isEmpty())
    }

    @Test
    fun testParseTreasures_validJson_returnsList() {
        val json = readJSON("treasures.json") ?: ""
        val treasures = helper.parseTreasures(json)
        assertNotNull(treasures)
        assertTrue("Parsed treasures list should not be empty", treasures.isNotEmpty())
    }

    @Test
    fun testParseTreasures_emptyJson_returnsEmptyList() {
        val treasures = helper.parseTreasures("")
        assertTrue(treasures.isEmpty())
    }

    @Test
    fun testCreateParams_buildsDungeonParamsWithParsedLists() {
        val monsterJson = readJSON("monsters.json") ?: ""
        val treasureJson = readJSON("treasures.json") ?: ""

        val params = helper.createParams(
            partyLevel = 2,
            partySize = 4,
            dungeonDifficulty = 1,
            treasureValue = 1.0,
            itemsRarity = 2,
            monsterType = "any",
            jsonMonster = monsterJson,
            jsonTreasure = treasureJson
        )

        assertEquals(2, params.partyLevel)
        assertEquals(4, params.partySize)
        assertTrue(params.monsterList.isNotEmpty())
        assertTrue(params.treasureList.isNotEmpty())
    }

    private fun getTiles(): Array<Array<DungeonTile>> {
        val max = 22
        val dungeon = Array(max) { i ->
            Array(max) { j ->
                DungeonTile(i, j)
            }
        }
        val imgSizeX = 20
        val imgSizeY = 20
        for (i in 1 until max - 1) {
            for (j in 1 until max - 1) {
                dungeon[i][j] = DungeonTile(
                    i,
                    j,
                    (j - 1) * imgSizeX,
                    (i - 1) * imgSizeY,
                    imgSizeX,
                    imgSizeY
                )
            }
        }
        return dungeon
    }

    companion object {
        @JvmStatic
        fun readJSON(resource: String): String? {
            return try {
                val isStream = MainActivity::class.java.classLoader?.getResourceAsStream(resource)
                isStream?.use { stream ->
                    Scanner(stream).use { scanner ->
                        val sb = StringBuilder()
                        while (scanner.hasNextLine()) {
                            sb.append(scanner.nextLine())
                        }
                        sb.toString()
                    }
                }
            } catch (ex: Exception) {
                Log.e("readJSON", ex.message ?: "")
                null
            }
        }

        @JvmStatic
        fun <T> twoDArrayToList(twoDArray: Array<Array<T>>): List<T> {
            val list = ArrayList<T>()
            for (array in twoDArray) {
                list.addAll(array)
            }
            return list
        }
    }
}
