package externius.rdmg.helpers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Monster
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import externius.rdmg.models.Treasures
import java.util.Random
import kotlin.math.floor

object Utils {
    @JvmStatic
    var partyLevel: Int = 0

    @JvmStatic
    var partySize: Int = 0

    @JvmStatic
    var dungeonDifficulty: Int = 0

    @JvmStatic
    var treasureValue: Double = 0.0

    @JvmStatic
    var itemsRarity: Int = 0

    @JvmStatic
    var monsterType: String = ""

    @JvmStatic
    var monsterList: List<Monster>? = null
        private set

    @JvmStatic
    var treasureList: List<Treasures>? = null
        private set

    private val gson = Gson()
    private val random = Random()
    private val monsterListType = object : TypeToken<ArrayList<Monster>>() {}.type
    private val treasureListType = object : TypeToken<ArrayList<Treasures>>() {}.type

    @JvmStatic
    val treasurePercentage: Int
        get() = when (dungeonDifficulty) {
            0 -> getRandomInt(20, 71)
            1 -> getRandomInt(30, 81)
            2 -> getRandomInt(40, 91)
            3 -> getRandomInt(50, 101)
            else -> 0
        }

    @JvmStatic
    val monsterPercentage: Int
        get() = when (dungeonDifficulty) {
            0 -> getRandomInt(40, 81)
            1 -> getRandomInt(50, 91)
            2 -> getRandomInt(60, 101)
            3 -> getRandomInt(70, 101)
            else -> 0
        }

    @JvmStatic
    fun addRoamingMonsterDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roamingMonsterDescription: MutableList<RoamingMonsterDescription>
    ) {
        dungeon[x][y].index = roamingMonsterDescription.size
        roamingMonsterDescription.add(
            RoamingMonsterDescription(
                Encounter.getRoamingName(roamingMonsterDescription.size + 1),
                Encounter.getRoamingMonster()
            )
        )
        dungeon[x][y].description = roamingMonsterDescription.size.toString()
    }

    @JvmStatic
    fun addTrapDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        trapDescription: MutableList<TrapDescription>
    ) {
        dungeon[x][y].index = trapDescription.size
        trapDescription.add(
            TrapDescription(
                Trap.getTrapName(trapDescription.size + 1),
                Trap.getCurrentTrap(false)
            )
        )
        dungeon[x][y].description = trapDescription.size.toString()
    }

    @JvmStatic
    fun addRoomDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roomDescription: MutableList<RoomDescription>,
        doorList: List<DungeonTile>
    ) {
        dungeon[x][y].index = roomDescription.size
        roomDescription.add(
            RoomDescription(
                getRoomName(roomDescription.size + 1),
                Treasure.getTreasure(),
                Encounter.getMonster(),
                Door.getDoorDescription(dungeon, doorList)
            )
        )
        dungeon[x][y].description = roomDescription.size.toString()
    }

    @JvmStatic
    fun addNCRoomDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roomDescription: MutableList<RoomDescription>,
        doors: String
    ) {
        dungeon[x][y].index = roomDescription.size
        roomDescription.add(
            RoomDescription(
                getRoomName(roomDescription.size + 1),
                Treasure.getTreasure(),
                Encounter.getMonster(),
                doors
            )
        )
        dungeon[x][y].description = roomDescription.size.toString()
    }

    @JvmStatic
    fun manhattan(dx: Int, dy: Int): Int {
        return dx + dy
    }

    @JvmStatic
    fun getRandomInt(min: Int, max: Int): Int {
        return if (max != min) {
            random.nextInt(max - min) + min
        } else {
            max
        }
    }

    private fun getRoomName(x: Int): String {
        return "#ROOM$x#"
    }

    private fun getMonsters(monsters: List<Monster>): List<Monster> {
        val result = mutableListOf<Monster>()
        if (monsterType.equals("any", ignoreCase = true)) {
            for (monster in monsters) {
                val cr = parse(monster.challengeRating ?: "0")
                if (cr <= partyLevel + 2 && cr >= floor((partyLevel / 4).toDouble())) {
                    result.add(monster)
                }
            }
        } else {
            for (monster in monsters) {
                val cr = parse(monster.challengeRating ?: "0")
                if (cr <= partyLevel + 2 && cr >= floor((partyLevel / 4).toDouble())
                    && checkType(monster.type)
                ) {
                    result.add(monster)
                }
            }
        }
        return result
    }

    private fun checkType(mType: String?): Boolean {
        if (mType == null) return false
        val types = monsterType.split(",")
        for (type in types) {
            if (mType.lowercase() == type) {
                return true
            }
        }
        return false
    }

    private fun parse(ratio: String): Double {
        return if (ratio.contains("/")) {
            val rat = ratio.split("/")
            rat[0].toDouble() / rat[1].toDouble()
        } else {
            ratio.toDouble()
        }
    }

    @JvmStatic
    fun setJsonMonster(jsonMonster: String) {
        val list: List<Monster>? = gson.fromJson(jsonMonster, monsterListType)
        monsterList = if (list != null) getMonsters(list) else emptyList()
    }

    @JvmStatic
    fun setJsonTreasure(jsonTreasure: String) {
        treasureList = gson.fromJson(jsonTreasure, treasureListType)
    }
}
