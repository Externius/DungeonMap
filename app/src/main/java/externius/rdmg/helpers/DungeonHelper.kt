package externius.rdmg.helpers

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import externius.rdmg.models.DungeonParams
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Monster
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import externius.rdmg.models.Treasures
import kotlinx.serialization.json.Json
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

@Singleton
class DungeonHelper @Inject constructor(
    private val json: Json = defaultJson,
    private val random: Random = Random()
) {
    fun parseMonsters(jsonMonster: String, partyLevel: Int, monsterType: String): List<Monster> {
        if (jsonMonster.isBlank()) return emptyList()
        val list: List<Monster> = try {
            json.decodeFromString<List<Monster>>(jsonMonster)
        } catch (e: Exception) {
            Log.e("DungeonHelper", "Failed to parse monsters JSON", e)
            emptyList()
        }
        return filterMonsters(list, partyLevel, monsterType)
    }

    fun parseTreasures(jsonTreasure: String): List<Treasures> {
        if (jsonTreasure.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Treasures>>(jsonTreasure)
        } catch (e: Exception) {
            Log.e("DungeonHelper", "Failed to parse treasures JSON", e)
            emptyList()
        }
    }

    fun createParams(
        partyLevel: Int,
        partySize: Int,
        dungeonDifficulty: Int,
        treasureValue: Double,
        itemsRarity: Int,
        monsterType: String,
        jsonMonster: String?,
        jsonTreasure: String?
    ): DungeonParams {
        val monsters = parseMonsters(jsonMonster ?: "", partyLevel, monsterType)
        val treasures = parseTreasures(jsonTreasure ?: "")
        return DungeonParams(
            partyLevel = partyLevel,
            partySize = partySize,
            dungeonDifficulty = dungeonDifficulty,
            treasureValue = treasureValue,
            itemsRarity = itemsRarity,
            monsterType = monsterType,
            monsterList = monsters,
            treasureList = treasures
        )
    }

    private fun filterMonsters(
        monsters: List<Monster>,
        partyLevel: Int,
        monsterType: String
    ): List<Monster> {
        val result = mutableListOf<Monster>()
        if (monsterType.equals("any", ignoreCase = true)) {
            for (monster in monsters) {
                val cr = parseChallengeRating(monster.challengeRating ?: "0")
                if (cr <= partyLevel + 2 && cr >= floor((partyLevel / 4).toDouble())) {
                    result.add(monster)
                }
            }
        } else {
            for (monster in monsters) {
                val cr = parseChallengeRating(monster.challengeRating ?: "0")
                if (cr <= partyLevel + 2 && cr >= floor((partyLevel / 4).toDouble())
                    && checkType(monster.type, monsterType)
                ) {
                    result.add(monster)
                }
            }
        }
        return result
    }

    private fun checkType(mType: String?, monsterType: String): Boolean {
        if (mType == null) return false
        val types = monsterType.split(",")
        for (type in types) {
            if (mType.equals(type.trim(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun parseChallengeRating(ratio: String): Double {
        return try {
            if (ratio.contains("/")) {
                val rat = ratio.split("/")
                val num = rat.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val den = rat.getOrNull(1)?.toDoubleOrNull() ?: 1.0
                if (den == 0.0) 0.0 else num / den
            } else {
                ratio.toDoubleOrNull() ?: 0.0
            }
        } catch (_: Exception) {
            0.0
        }
    }

    fun getRandomInt(min: Int, max: Int): Int {
        if (min >= max) return min
        return random.nextInt(max - min) + min
    }

    fun manhattan(dx: Int, dy: Int): Int {
        return dx + dy
    }

    fun addRoamingMonsterDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roamingMonsterDescription: MutableList<RoamingMonsterDescription>,
        params: DungeonParams
    ) {
        dungeon[x][y].index = roamingMonsterDescription.size
        roamingMonsterDescription.add(
            RoamingMonsterDescription(
                Encounter.getRoamingName(roamingMonsterDescription.size + 1),
                Encounter.getRoamingMonster(params)
            )
        )
        dungeon[x][y].description = roamingMonsterDescription.size.toString()
    }

    fun addTrapDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        trapDescription: MutableList<TrapDescription>,
        dungeonDifficulty: Int,
        partyLevel: Int
    ) {
        dungeon[x][y].index = trapDescription.size
        trapDescription.add(
            TrapDescription(
                Trap.getTrapName(trapDescription.size + 1),
                Trap.getCurrentTrap(false, dungeonDifficulty, partyLevel)
            )
        )
        dungeon[x][y].description = trapDescription.size.toString()
    }

    fun addRoomDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roomDescription: MutableList<RoomDescription>,
        doorList: List<DungeonTile>,
        params: DungeonParams
    ) {
        dungeon[x][y].index = roomDescription.size
        roomDescription.add(
            RoomDescription(
                getRoomName(roomDescription.size + 1),
                Treasure.getTreasure(params),
                Encounter.getMonster(params),
                Door.getDoorDescription(
                    dungeon,
                    doorList,
                    params.dungeonDifficulty,
                    params.partyLevel
                )
            )
        )
        dungeon[x][y].description = roomDescription.size.toString()
    }

    fun addNCRoomDescription(
        dungeon: Array<Array<DungeonTile>>,
        x: Int,
        y: Int,
        roomDescription: MutableList<RoomDescription>,
        doors: String,
        params: DungeonParams
    ) {
        dungeon[x][y].index = roomDescription.size
        roomDescription.add(
            RoomDescription(
                getRoomName(roomDescription.size + 1),
                Treasure.getTreasure(params),
                Encounter.getMonster(params),
                doors
            )
        )
        dungeon[x][y].description = roomDescription.size.toString()
    }

    private fun getRoomName(x: Int): String {
        return "#ROOM$x#"
    }

    companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }

        private val defaultHelper = DungeonHelper()

        fun getRandomInt(min: Int, max: Int): Int = defaultHelper.getRandomInt(min, max)

        fun manhattan(dx: Int, dy: Int): Int = defaultHelper.manhattan(dx, dy)

        fun createParams(
            partyLevel: Int,
            partySize: Int,
            dungeonDifficulty: Int,
            treasureValue: Double,
            itemsRarity: Int,
            monsterType: String,
            jsonMonster: String?,
            jsonTreasure: String?
        ): DungeonParams = defaultHelper.createParams(
            partyLevel,
            partySize,
            dungeonDifficulty,
            treasureValue,
            itemsRarity,
            monsterType,
            jsonMonster,
            jsonTreasure
        )

        fun addRoamingMonsterDescription(
            dungeon: Array<Array<DungeonTile>>,
            x: Int,
            y: Int,
            roamingMonsterDescription: MutableList<RoamingMonsterDescription>,
            params: DungeonParams
        ) = defaultHelper.addRoamingMonsterDescription(
            dungeon,
            x,
            y,
            roamingMonsterDescription,
            params
        )

        fun addTrapDescription(
            dungeon: Array<Array<DungeonTile>>,
            x: Int,
            y: Int,
            trapDescription: MutableList<TrapDescription>,
            dungeonDifficulty: Int,
            partyLevel: Int
        ) = defaultHelper.addTrapDescription(
            dungeon,
            x,
            y,
            trapDescription,
            dungeonDifficulty,
            partyLevel
        )

        fun addRoomDescription(
            dungeon: Array<Array<DungeonTile>>,
            x: Int,
            y: Int,
            roomDescription: MutableList<RoomDescription>,
            doorList: List<DungeonTile>,
            params: DungeonParams
        ) = defaultHelper.addRoomDescription(dungeon, x, y, roomDescription, doorList, params)

        fun addNCRoomDescription(
            dungeon: Array<Array<DungeonTile>>,
            x: Int,
            y: Int,
            roomDescription: MutableList<RoomDescription>,
            doors: String,
            params: DungeonParams
        ) = defaultHelper.addNCRoomDescription(dungeon, x, y, roomDescription, doors, params)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object HelperModule {

    @Provides
    @Singleton
    fun provideJson(): Json = DungeonHelper.defaultJson

    @Provides
    @Singleton
    fun provideRandom(): Random = Random()
}
