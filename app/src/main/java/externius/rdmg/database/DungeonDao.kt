package externius.rdmg.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class DungeonSummary(
    @ColumnInfo(name = DungeonConstants.DUNGEON_ID) val id: Long,
    @ColumnInfo(name = DungeonConstants.DUNGEON_NAME) val dungeonName: String?,
    @ColumnInfo(name = DungeonConstants.DUNGEON_CREATED) val dungeonCreated: String?
)

@Dao
interface DungeonDao {
    @Query("SELECT * FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    suspend fun getAllDungeons(): List<DungeonEntity>

    @Query("SELECT _id, DUNGEON_NAME, DUNGEON_CREATED FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    suspend fun getDungeonSummaries(): List<DungeonSummary>

    @Query("SELECT _id, DUNGEON_NAME, DUNGEON_CREATED FROM dungeons ORDER BY DUNGEON_CREATED DESC, _id DESC")
    fun getDungeonSummariesFlow(): Flow<List<DungeonSummary>>

    @Query("SELECT * FROM dungeons WHERE _id = :id")
    suspend fun getDungeonById(id: Long): DungeonEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(dungeon: DungeonEntity): Long

    @Update
    suspend fun update(dungeon: DungeonEntity): Int

    @Query("DELETE FROM dungeons WHERE _id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM dungeons")
    suspend fun deleteAll(): Int
}
