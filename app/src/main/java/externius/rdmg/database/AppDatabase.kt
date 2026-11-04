package externius.rdmg.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DungeonEntity::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dungeonDao(): DungeonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `dungeons_new` (
                        `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `DUNGEON_NAME` TEXT,
                        `LOADED_DUNGEON` TEXT NOT NULL,
                        `LOADED_ROOM_DESCRIPTION` TEXT,
                        `LOADED_TRAP_DESCRIPTION` TEXT,
                        `LOADED_ROAMING_MONSTERS` TEXT,
                        `DUNGEON_DIFFICULTY` TEXT,
                        `PARTY_LEVEL` TEXT,
                        `PARTY_SIZE` TEXT,
                        `TREASURE_VALUE` TEXT,
                        `ITEMS_RARITY` TEXT,
                        `DUNGEON_SIZE` TEXT,
                        `ROOM_DENSITY` TEXT,
                        `ROOM_SIZE` TEXT,
                        `TRAPS` TEXT,
                        `CORRIDORS` TEXT,
                        `MONSTER_TYPE` TEXT,
                        `DEAD_ENDS` TEXT,
                        `ROAMING_MONSTERS` TEXT,
                        `DUNGEON_CREATED` TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `dungeons_new` (
                        `_id`, `DUNGEON_NAME`, `LOADED_DUNGEON`, `LOADED_ROOM_DESCRIPTION`,
                        `LOADED_TRAP_DESCRIPTION`, `LOADED_ROAMING_MONSTERS`, `DUNGEON_DIFFICULTY`,
                        `PARTY_LEVEL`, `PARTY_SIZE`, `TREASURE_VALUE`, `ITEMS_RARITY`, `DUNGEON_SIZE`,
                        `ROOM_DENSITY`, `ROOM_SIZE`, `TRAPS`, `CORRIDORS`, `MONSTER_TYPE`, `DEAD_ENDS`,
                        `ROAMING_MONSTERS`, `DUNGEON_CREATED`
                    )
                    SELECT 
                        `_id`, `DUNGEON_NAME`, `LOADED_DUNGEON`, `LOADED_ROOM_DESCRIPTION`,
                        `LOADED_TRAP_DESCRIPTION`, `LOADED_ROAMING_MONSTERS`, `DUNGEON_DIFFICULTY`,
                        `PARTY_LEVEL`, `PARTY_SIZE`, `TREASURE_VALUE`, `ITEMS_RARITY`, `DUNGEON_SIZE`,
                        `ROOM_DENSITY`, `ROOM_SIZE`, `TRAPS`, `CORRIDORS`, `MONSTER_TYPE`, `DEAD_ENDS`,
                        `ROAMING_MONSTERS`, COALESCE(`DUNGEON_CREATED`, CURRENT_TIMESTAMP)
                    FROM `dungeons`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `dungeons`")
                db.execSQL("ALTER TABLE `dungeons_new` RENAME TO `dungeons`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dungeons_LOADED_DUNGEON` ON `dungeons` (`LOADED_DUNGEON`)")
            }
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE dungeons ADD COLUMN ROAMING_MONSTERS TEXT;")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE dungeons ADD COLUMN LOADED_ROAMING_MONSTERS TEXT;")
                } catch (_: Exception) {
                }
                MIGRATION_3_4.migrate(db)
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE dungeons ADD COLUMN LOADED_ROAMING_MONSTERS TEXT;")
                } catch (_: Exception) {
                }
                MIGRATION_3_4.migrate(db)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dungeons.db"
                )
                    .addMigrations(MIGRATION_1_4, MIGRATION_2_4, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
