package externius.rdmg

import externius.rdmg.core.DungeonNoCorridor
import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class DungeonNoCorridorTest {
    private val dungeonNoCorridor = DungeonNoCorridor(800, 800, 15, 15)

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
        dungeonNoCorridor.init()
    }

    @Test
    fun testInit() {
        println(name.methodName)
        DrawTestDungeon.draw(dungeonNoCorridor.dungeonTiles)
        assertTrue(dungeonNoCorridor.roomDescription.isEmpty())
    }

    @Test
    fun testAddFirstRoom() {
        dungeonNoCorridor.addFirstRoom()
        println(name.methodName)
        DrawTestDungeon.draw(dungeonNoCorridor.dungeonTiles)
        assertTrue(dungeonNoCorridor.roomDescription.isEmpty())
        assertEquals(2, dungeonNoCorridor.openDoorList.size)
    }

    @Test
    fun testFillRoomToDoor() {
        dungeonNoCorridor.addFirstRoom()
        dungeonNoCorridor.fillRoomToDoor()
        println(name.methodName)
        DrawTestDungeon.draw(dungeonNoCorridor.dungeonTiles)
        assertEquals(0, dungeonNoCorridor.openDoorList.size)
    }

    @Test
    fun testAddDescription() {
        dungeonNoCorridor.addFirstRoom()
        dungeonNoCorridor.fillRoomToDoor()
        dungeonNoCorridor.addDescription()
        println(name.methodName)
        DrawTestDungeon.draw(dungeonNoCorridor.dungeonTiles)
        assertThat(
            dungeonNoCorridor.roomDescription,
            allOf(hasSize(greaterThan(1)), hasSize(lessThan(30)))
        )
    }

    @Test
    fun testAddEntryPoint() {
        dungeonNoCorridor.addFirstRoom()
        dungeonNoCorridor.fillRoomToDoor()
        dungeonNoCorridor.addDescription()
        dungeonNoCorridor.addEntryPoint()
        println(name.methodName)
        DrawTestDungeon.draw(dungeonNoCorridor.dungeonTiles)
        val dungeonList = UtilsTest.twoDArrayToList(dungeonNoCorridor.dungeonTiles)
        assertThat(
            dungeonList,
            hasItem(hasProperty<DungeonTile>("texture", equalTo(Textures.ENTRY)))
        )
    }
}
