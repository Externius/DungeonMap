package externius.rdmg.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dungeons.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_DUNGEONS = "dungeons";
    public static final String DUNGEON_ID = "_id";
    public static final String DUNGEON_NAME = "DUNGEON_NAME";
    public static final String LOADED_DUNGEON = "LOADED_DUNGEON";
    public static final String LOADED_ROOM_DESCRIPTION = "LOADED_ROOM_DESCRIPTION";
    public static final String LOADED_TRAP_DESCRIPTION = "LOADED_TRAP_DESCRIPTION";
    public static final String DUNGEON_DIFFICULTY = "DUNGEON_DIFFICULTY";
    public static final String PARTY_LEVEL = "PARTY_LEVEL";
    public static final String PARTY_SIZE = "PARTY_SIZE";
    public static final String TREASURE_VALUE = "TREASURE_VALUE";
    public static final String ITEMS_RARITY = "ITEMS_RARITY";
    public static final String DUNGEON_SIZE = "DUNGEON_SIZE";
    public static final String ROOM_DENSITY = "ROOM_DENSITY";
    public static final String ROOM_SIZE = "ROOM_SIZE";
    public static final String TRAPS = "TRAPS";
    public static final String CORRIDORS = "CORRIDORS";
    public static final String MONSTER_TYPE = "MONSTER_TYPE";
    public static final String DEAD_ENDS = "DEAD_ENDS";
    static final String DUNGEON_CREATED = "DUNGEON_CREATED";

    public static final String[] ALL_COLUMNS = {DUNGEON_ID, DUNGEON_NAME, LOADED_DUNGEON, LOADED_ROOM_DESCRIPTION, LOADED_TRAP_DESCRIPTION,
            DUNGEON_DIFFICULTY, PARTY_LEVEL, PARTY_SIZE, TREASURE_VALUE, ITEMS_RARITY, DUNGEON_SIZE, ROOM_DENSITY, ROOM_SIZE,
            TRAPS, CORRIDORS, MONSTER_TYPE, DEAD_ENDS, DUNGEON_CREATED};

    public static final String[] TO_BUNDLE = {
            DUNGEON_DIFFICULTY, PARTY_LEVEL, PARTY_SIZE, TREASURE_VALUE, ITEMS_RARITY, DUNGEON_SIZE, ROOM_DENSITY, ROOM_SIZE,
            TRAPS, CORRIDORS, MONSTER_TYPE, DEAD_ENDS};

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_DUNGEONS + " (" +
                    DUNGEON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DUNGEON_NAME + " TEXT, " +
                    LOADED_DUNGEON + " TEXT NOT NULL UNIQUE, " +
                    LOADED_ROOM_DESCRIPTION + " TEXT, " +
                    LOADED_TRAP_DESCRIPTION + " TEXT, " +
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
                    DUNGEON_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DUNGEONS);
        onCreate(sqLiteDatabase);
    }
}
