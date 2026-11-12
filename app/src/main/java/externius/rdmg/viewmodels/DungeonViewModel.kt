package externius.rdmg.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.EditPopupState
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DungeonViewModel(
    application: Application,
    val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val dungeonDao = AppDatabase.getInstance(application).dungeonDao()

    var loadedDungeon: Array<Array<DungeonTile>>? = null
    var loadedRoomDescription: MutableList<RoomDescription>? = null
    var loadedTrapDescription: MutableList<TrapDescription>? = null
    var loadedRoamingMonsterDescription: MutableList<RoamingMonsterDescription>? = null
    var dungeonId: Long = -1L
    var loadedEntity: DungeonEntity? = null

    var editPopupState: EditPopupState?
        get() = savedStateHandle.get<EditPopupState>(KEY_EDIT_POPUP_STATE)
        set(value) {
            savedStateHandle[KEY_EDIT_POPUP_STATE] = value
        }

    suspend fun getDungeonById(id: Long): DungeonEntity? = withContext(Dispatchers.IO) {
        dungeonDao.getDungeonById(id)
    }

    suspend fun updateDungeon(entity: DungeonEntity): Int = withContext(Dispatchers.IO) {
        dungeonDao.update(entity)
    }

    suspend fun insertDungeon(entity: DungeonEntity): Long = withContext(Dispatchers.IO) {
        dungeonDao.insert(entity)
    }

    companion object {
        private const val KEY_EDIT_POPUP_STATE = "edit_popup_state"
    }
}
