package externius.rdmg

import android.util.Log
import externius.rdmg.activities.MainActivity
import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonParams
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.util.Scanner

class UtilsTest {

    private lateinit var params: DungeonParams

    @get:Rule
    val name = TestName()

    @Before
    fun setup() {
        params = Utils.createParams(
            partyLevel = 1,
            partySize = 4,
            dungeonDifficulty = 1,
            treasureValue = 1.0,
            itemsRarity = 3,
            monsterType = "any",
            jsonMonster = readJSON("monsters.json") ?: "",
            jsonTreasure = readJSON("treasures.json") ?: ""
        )
    }

    @Test
    fun testManhattan() {
        val x = Utils.manhattan(2, 5)
        assertEquals(name.methodName + " not adding correctly", 7, x)
    }

    @Test
    fun testGetRandomInt() {
        repeat(100) {
            val x = Utils.getRandomInt(2, 9)
            assertNotEquals(name.methodName + " returns lower value then expected", 1, x)
            assertNotEquals(name.methodName + " returns the max value", 9, x)
        }
        val y = Utils.getRandomInt(2, 2)
        assertEquals(name.methodName + " not working correctly if the 2 parameters equals", 2, y)
    }

    @Test
    fun testAddTrapDescription() {
        val dungeon = getTiles()
        val trapDescription = ArrayList<TrapDescription>()
        for (i in 0 until 3) {
            Utils.addTrapDescription(
                dungeon,
                i + 1,
                i + 1,
                trapDescription,
                params.dungeonDifficulty,
                params.partyLevel
            )
        }
        assertEquals(name.methodName + " size isn't right", 3, trapDescription.size)
    }

    @Test
    fun testAddRoomDescription() {
        val dungeon = getTiles()
        val roomDescription = ArrayList<RoomDescription>()
        val doors = ArrayList<DungeonTile>()
        doors.add(DungeonTile(3, 3, 50, 50, 50, 50))
        dungeon[3][3].texture = Textures.DOOR
        dungeon[3][4].texture = Textures.ROOM
        for (i in 0 until 3) {
            Utils.addRoomDescription(dungeon, i + 1, i + 1, roomDescription, doors, params)
        }
        assertEquals(name.methodName + " size isn't right", 3, roomDescription.size)
    }

    @Test
    fun testAddNCRoomDescription() {
        val dungeon = getTiles()
        val roomDescription = ArrayList<RoomDescription>()
        Utils.addNCRoomDescription(dungeon, 5, 5, roomDescription, "Example door text", params)
        assertEquals(name.methodName + " size isn't right", 1, roomDescription.size)
    }

    companion object {
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
