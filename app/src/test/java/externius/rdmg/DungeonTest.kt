package externius.rdmg

import externius.rdmg.core.Dungeon
import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class DungeonTest {

    private val dungeon = Dungeon(800, 800, 15, 10, 15, 15, true, 10)

    @get:Rule
    val name = TestName()

    @Before
    fun setup() {
        Utils.dungeonDifficulty = 1
        Utils.partySize = 4
        Utils.partyLevel = 1
        Utils.monsterType = "any"
        Utils.treasureValue = 1.0
        Utils.itemsRarity = 3
        Utils.setJsonMonster(UtilsTest.readJSON("monsters.json") ?: "")
        Utils.setJsonTreasure(UtilsTest.readJSON("treasures.json") ?: "")
        dungeon.init()
    }

    @Test
    fun testInit() {
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        assertTrue(dungeon.roomDescription.isEmpty())
    }

    @Test
    fun testGenerateRoom() {
        dungeon.generateRoom()
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        assertEquals(2, dungeon.roomDescription.size)
    }

    @Test
    fun testAddEntryPoint() {
        dungeon.generateRoom()
        dungeon.addEntryPoint()
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeon.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.ENTRY)))
        )
    }

    @Test
    fun testGenerateCorridors() {
        dungeon.generateRoom()
        dungeon.addEntryPoint()
        dungeon.generateCorridors()
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeon.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.CORRIDOR)))
        )
    }

    @Test
    fun testAddDeadEnds() {
        dungeon.generateRoom()
        dungeon.addEntryPoint()
        dungeon.generateCorridors()
        dungeon.addDeadEnds()
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeon.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.CORRIDOR)))
        )
    }

    @Test
    fun testAddTrap() {
        dungeon.generateRoom()
        dungeon.addEntryPoint()
        dungeon.generateCorridors()
        dungeon.addDeadEnds()
        dungeon.addCorridorItem(2, Dungeon.Item.TRAP)
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeon.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.TRAP)))
        )
    }

    @Test
    fun testAddRoamingMonster() {
        dungeon.generateRoom()
        dungeon.addEntryPoint()
        dungeon.generateCorridors()
        dungeon.addDeadEnds()
        dungeon.addCorridorItem(2, Dungeon.Item.ROAMING_MONSTER)
        println(name.methodName)
        DrawTestDungeon.draw(dungeon.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeon.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.ROAMING_MONSTER)))
        )
    }
}
