package externius.rdmg.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = DungeonConstants.TABLE_DUNGEONS,
    indices = [Index(value = [DungeonConstants.LOADED_DUNGEON], unique = true)]
)
data class DungeonEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DungeonConstants.DUNGEON_ID)
    val id: Long = 0,

    @ColumnInfo(name = DungeonConstants.DUNGEON_NAME)
    val dungeonName: String? = null,

    @ColumnInfo(name = DungeonConstants.LOADED_DUNGEON)
    val loadedDungeon: String,

    @ColumnInfo(name = DungeonConstants.LOADED_ROOM_DESCRIPTION)
    val loadedRoomDescription: String? = null,

    @ColumnInfo(name = DungeonConstants.LOADED_TRAP_DESCRIPTION)
    val loadedTrapDescription: String? = null,

    @ColumnInfo(name = DungeonConstants.LOADED_ROAMING_MONSTERS)
    val loadedRoamingMonsters: String? = null,

    @ColumnInfo(name = DungeonConstants.DUNGEON_DIFFICULTY)
    val dungeonDifficulty: String? = null,

    @ColumnInfo(name = DungeonConstants.PARTY_LEVEL)
    val partyLevel: String? = null,

    @ColumnInfo(name = DungeonConstants.PARTY_SIZE)
    val partySize: String? = null,

    @ColumnInfo(name = DungeonConstants.TREASURE_VALUE)
    val treasureValue: String? = null,

    @ColumnInfo(name = DungeonConstants.ITEMS_RARITY)
    val itemsRarity: String? = null,

    @ColumnInfo(name = DungeonConstants.DUNGEON_SIZE)
    val dungeonSize: String? = null,

    @ColumnInfo(name = DungeonConstants.ROOM_DENSITY)
    val roomDensity: String? = null,

    @ColumnInfo(name = DungeonConstants.ROOM_SIZE)
    val roomSize: String? = null,

    @ColumnInfo(name = DungeonConstants.TRAPS)
    val traps: String? = null,

    @ColumnInfo(name = DungeonConstants.CORRIDORS)
    val corridors: String? = null,

    @ColumnInfo(name = DungeonConstants.MONSTER_TYPE)
    val monsterType: String? = null,

    @ColumnInfo(name = DungeonConstants.DEAD_ENDS)
    val deadEnds: String? = null,

    @ColumnInfo(name = DungeonConstants.ROAMING_MONSTERS)
    val roamingMonsters: String? = null,

    @ColumnInfo(name = DungeonConstants.DUNGEON_CREATED, defaultValue = "CURRENT_TIMESTAMP")
    val dungeonCreated: String? = null
)
