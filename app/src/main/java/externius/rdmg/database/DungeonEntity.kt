package externius.rdmg.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = DBOpenHelper.TABLE_DUNGEONS,
    indices = [Index(value = ["LOADED_DUNGEON"], unique = true)]
)
data class DungeonEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBOpenHelper.DUNGEON_ID)
    val id: Long = 0,

    @ColumnInfo(name = DBOpenHelper.DUNGEON_NAME)
    val dungeonName: String? = null,

    @ColumnInfo(name = DBOpenHelper.LOADED_DUNGEON)
    val loadedDungeon: String,

    @ColumnInfo(name = DBOpenHelper.LOADED_ROOM_DESCRIPTION)
    val loadedRoomDescription: String? = null,

    @ColumnInfo(name = DBOpenHelper.LOADED_TRAP_DESCRIPTION)
    val loadedTrapDescription: String? = null,

    @ColumnInfo(name = DBOpenHelper.LOADED_ROAMING_MONSTERS)
    val loadedRoamingMonsters: String? = null,

    @ColumnInfo(name = DBOpenHelper.DUNGEON_DIFFICULTY)
    val dungeonDifficulty: String? = null,

    @ColumnInfo(name = DBOpenHelper.PARTY_LEVEL)
    val partyLevel: String? = null,

    @ColumnInfo(name = DBOpenHelper.PARTY_SIZE)
    val partySize: String? = null,

    @ColumnInfo(name = DBOpenHelper.TREASURE_VALUE)
    val treasureValue: String? = null,

    @ColumnInfo(name = DBOpenHelper.ITEMS_RARITY)
    val itemsRarity: String? = null,

    @ColumnInfo(name = DBOpenHelper.DUNGEON_SIZE)
    val dungeonSize: String? = null,

    @ColumnInfo(name = DBOpenHelper.ROOM_DENSITY)
    val roomDensity: String? = null,

    @ColumnInfo(name = DBOpenHelper.ROOM_SIZE)
    val roomSize: String? = null,

    @ColumnInfo(name = DBOpenHelper.TRAPS)
    val traps: String? = null,

    @ColumnInfo(name = DBOpenHelper.CORRIDORS)
    val corridors: String? = null,

    @ColumnInfo(name = DBOpenHelper.MONSTER_TYPE)
    val monsterType: String? = null,

    @ColumnInfo(name = DBOpenHelper.DEAD_ENDS)
    val deadEnds: String? = null,

    @ColumnInfo(name = DBOpenHelper.ROAMING_MONSTERS)
    val roamingMonsters: String? = null,

    @ColumnInfo(name = DBOpenHelper.DUNGEON_CREATED, defaultValue = "CURRENT_TIMESTAMP")
    val dungeonCreated: String? = null
)
