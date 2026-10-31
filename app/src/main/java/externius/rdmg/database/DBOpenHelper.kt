package externius.rdmg.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper internal constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_DUNGEONS")
            onCreate(sqLiteDatabase)
        } else if (oldVersion == 2) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_1)
            sqLiteDatabase.execSQL(DATABASE_ALTER_2)
        }
    }

    companion object {
        private const val DATABASE_NAME = "dungeons.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_DUNGEONS = "dungeons"
        const val DUNGEON_ID = "_id"
        const val DUNGEON_NAME = "DUNGEON_NAME"
        const val LOADED_DUNGEON = "LOADED_DUNGEON"
        const val LOADED_ROOM_DESCRIPTION = "LOADED_ROOM_DESCRIPTION"
        const val LOADED_TRAP_DESCRIPTION = "LOADED_TRAP_DESCRIPTION"
        const val LOADED_ROAMING_MONSTERS = "LOADED_ROAMING_MONSTERS"
        const val DUNGEON_DIFFICULTY = "DUNGEON_DIFFICULTY"
        const val PARTY_LEVEL = "PARTY_LEVEL"
        const val PARTY_SIZE = "PARTY_SIZE"
        const val TREASURE_VALUE = "TREASURE_VALUE"
        const val ITEMS_RARITY = "ITEMS_RARITY"
        const val DUNGEON_SIZE = "DUNGEON_SIZE"
        const val ROOM_DENSITY = "ROOM_DENSITY"
        const val ROOM_SIZE = "ROOM_SIZE"
        const val TRAPS = "TRAPS"
        const val CORRIDORS = "CORRIDORS"
        const val MONSTER_TYPE = "MONSTER_TYPE"
        const val DEAD_ENDS = "DEAD_ENDS"
        const val ROAMING_MONSTERS = "ROAMING_MONSTERS"
        const val DUNGEON_CREATED = "DUNGEON_CREATED"

        @JvmField
        val ALL_COLUMNS = arrayOf(
            DUNGEON_ID, DUNGEON_NAME, LOADED_DUNGEON, LOADED_ROOM_DESCRIPTION,
            LOADED_TRAP_DESCRIPTION, LOADED_ROAMING_MONSTERS, DUNGEON_DIFFICULTY,
            PARTY_LEVEL, PARTY_SIZE, TREASURE_VALUE, ITEMS_RARITY, DUNGEON_SIZE,
            ROOM_DENSITY, ROOM_SIZE, TRAPS, CORRIDORS, MONSTER_TYPE, DEAD_ENDS,
            ROAMING_MONSTERS, DUNGEON_CREATED
        )

        @JvmField
        val TO_BUNDLE = arrayOf(
            DUNGEON_DIFFICULTY, PARTY_LEVEL, PARTY_SIZE, TREASURE_VALUE, ITEMS_RARITY,
            DUNGEON_SIZE, ROOM_DENSITY, ROOM_SIZE, TRAPS, CORRIDORS, MONSTER_TYPE,
            DEAD_ENDS, ROAMING_MONSTERS
        )

        private const val TABLE_CREATE =
            "CREATE TABLE " + TABLE_DUNGEONS + " (" +
                    DUNGEON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DUNGEON_NAME + " TEXT, " +
                    LOADED_DUNGEON + " TEXT NOT NULL UNIQUE, " +
                    LOADED_ROOM_DESCRIPTION + " TEXT, " +
                    LOADED_TRAP_DESCRIPTION + " TEXT, " +
                    LOADED_ROAMING_MONSTERS + " TEXT, " +
                    DUNGEON_DIFFICULTY + " TEXT, " +
                    PARTY_LEVEL + " TEXT, " +
                    PARTY_SIZE + " TEXT, " +
                    TREASURE_VALUE + " TEXT, " +
                    ITEMS_RARITY + " TEXT, " +
                    DUNGEON_SIZE + " TEXT, " +
                    ROOM_DENSITY + " TEXT, " +
                    ROOM_SIZE + " TEXT, " +
                    TRAPS + " TEXT, " +
                    CORRIDORS + " TEXT, " +
                    MONSTER_TYPE + " TEXT, " +
                    DEAD_ENDS + " TEXT, " +
                    ROAMING_MONSTERS + " TEXT, " +
                    DUNGEON_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")"

        private const val DATABASE_ALTER_1 =
            "ALTER TABLE $TABLE_DUNGEONS ADD COLUMN $ROAMING_MONSTERS TEXT;"

        private const val DATABASE_ALTER_2 =
            "ALTER TABLE $TABLE_DUNGEONS ADD COLUMN $LOADED_ROAMING_MONSTERS TEXT;"
    }
}
