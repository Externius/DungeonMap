package externius.rdmg.database

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DungeonDao {
    @Query("SELECT * FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getAllDungeons(): List<DungeonEntity>

    @Query("SELECT * FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getAllDungeonsCursor(): Cursor

    @Query("SELECT * FROM dungeons WHERE _id = :id")
    fun getDungeonById(id: Long): DungeonEntity?

    @Query("SELECT * FROM dungeons WHERE _id = :id")
    fun getDungeonCursorById(id: Long): Cursor

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(dungeon: DungeonEntity): Long

    @Update
    fun update(dungeon: DungeonEntity): Int

    @Query("DELETE FROM dungeons WHERE _id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM dungeons")
    fun deleteAll(): Int
}
