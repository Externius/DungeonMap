package externius.rdmg.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class DungeonSummary(
    @ColumnInfo(name = DBOpenHelper.DUNGEON_ID) val id: Long,
    @ColumnInfo(name = DBOpenHelper.DUNGEON_NAME) val dungeonName: String?,
    @ColumnInfo(name = DBOpenHelper.DUNGEON_CREATED) val dungeonCreated: String?
)

@Dao
interface DungeonDao {
    @Query("SELECT * FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getAllDungeons(): List<DungeonEntity>

    @Query("SELECT _id, DUNGEON_NAME, DUNGEON_CREATED FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getDungeonSummaries(): List<DungeonSummary>

    @Query("SELECT _id, DUNGEON_NAME, DUNGEON_CREATED FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getDungeonSummariesFlow(): Flow<List<DungeonSummary>>

    @Query("SELECT * FROM dungeons WHERE _id = :id")
    fun getDungeonById(id: Long): DungeonEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(dungeon: DungeonEntity): Long

    @Update
    fun update(dungeon: DungeonEntity): Int

    @Query("DELETE FROM dungeons WHERE _id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM dungeons")
    fun deleteAll(): Int
}
