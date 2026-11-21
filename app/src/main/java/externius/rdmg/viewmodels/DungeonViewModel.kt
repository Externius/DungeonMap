package externius.rdmg.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import externius.rdmg.database.DungeonDao
import externius.rdmg.database.DungeonEntity
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.EditPopupState
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.TrapDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DungeonUiState(
    val dungeonId: Long = -1L,
    val loadedEntity: DungeonEntity? = null,
    val loadedDungeon: List<List<DungeonTile>>? = null,
    val loadedRoomDescription: List<RoomDescription>? = null,
    val loadedTrapDescription: List<TrapDescription>? = null,
    val loadedRoamingMonsterDescription: List<RoamingMonsterDescription>? = null,
)

@HiltViewModel
class DungeonViewModel @Inject constructor(
    private val dungeonDao: DungeonDao,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DungeonUiState())
    val uiState: StateFlow<DungeonUiState> = _uiState.asStateFlow()

    var dungeonId: Long
        get() = _uiState.value.dungeonId
        set(value) {
            _uiState.update { it.copy(dungeonId = value) }
        }

    var loadedEntity: DungeonEntity?
        get() = _uiState.value.loadedEntity
        set(value) {
            _uiState.update { it.copy(loadedEntity = value) }
        }

    private var cachedDungeonList: List<List<DungeonTile>>? = null
    private var cachedDungeonArray: Array<Array<DungeonTile>>? = null

    var loadedDungeon: Array<Array<DungeonTile>>?
        get() {
            val currentList = _uiState.value.loadedDungeon ?: return null
            if (currentList === cachedDungeonList && cachedDungeonArray != null) {
                return cachedDungeonArray?.map { it.clone() }?.toTypedArray()
            }
            cachedDungeonList = currentList
            val array = currentList.map { it.toTypedArray() }.toTypedArray()
            cachedDungeonArray = array
            return array.map { it.clone() }.toTypedArray()
        }
        set(value) {
            val list = value?.map { row -> row.toList() }
            cachedDungeonList = list
            cachedDungeonArray = value?.map { it.clone() }?.toTypedArray()
            _uiState.update { it.copy(loadedDungeon = list) }
        }

    var loadedRoomDescription: List<RoomDescription>?
        get() = _uiState.value.loadedRoomDescription
        set(value) {
            _uiState.update { it.copy(loadedRoomDescription = value?.toList()) }
        }

    var loadedTrapDescription: List<TrapDescription>?
        get() = _uiState.value.loadedTrapDescription
        set(value) {
            _uiState.update { it.copy(loadedTrapDescription = value?.toList()) }
        }

    var loadedRoamingMonsterDescription: List<RoamingMonsterDescription>?
        get() = _uiState.value.loadedRoamingMonsterDescription
        set(value) {
            _uiState.update { it.copy(loadedRoamingMonsterDescription = value?.toList()) }
        }

    var editPopupState: EditPopupState?
        get() = savedStateHandle.get<EditPopupState>(KEY_EDIT_POPUP_STATE)
        set(value) {
            savedStateHandle[KEY_EDIT_POPUP_STATE] = value
        }

    fun updateRoomDescriptionAt(index: Int, room: RoomDescription) {
        _uiState.update { state ->
            val list = state.loadedRoomDescription?.toMutableList() ?: return@update state
            if (index in list.indices) {
                list[index] = room
                state.copy(loadedRoomDescription = list.toList())
            } else {
                state
            }
        }
    }

    fun updateTrapDescriptionAt(index: Int, trap: TrapDescription) {
        _uiState.update { state ->
            val list = state.loadedTrapDescription?.toMutableList() ?: return@update state
            if (index in list.indices) {
                list[index] = trap
                state.copy(loadedTrapDescription = list.toList())
            } else {
                state
            }
        }
    }

    fun updateRoamingMonsterDescriptionAt(index: Int, monster: RoamingMonsterDescription) {
        _uiState.update { state ->
            val list = state.loadedRoamingMonsterDescription?.toMutableList() ?: return@update state
            if (index in list.indices) {
                list[index] = monster
                state.copy(loadedRoamingMonsterDescription = list.toList())
            } else {
                state
            }
        }
    }

    fun setLoadedDungeonData(
        dungeonId: Long,
        entity: DungeonEntity,
        dungeon: Array<Array<DungeonTile>>?,
        rooms: List<RoomDescription>?,
        traps: List<TrapDescription>?,
        monsters: List<RoamingMonsterDescription>?
    ) {
        val list = dungeon?.map { row -> row.toList() }
        cachedDungeonList = list
        cachedDungeonArray = dungeon?.map { it.clone() }?.toTypedArray()
        _uiState.update {
            it.copy(
                dungeonId = dungeonId,
                loadedEntity = entity,
                loadedDungeon = list,
                loadedRoomDescription = rooms?.toList(),
                loadedTrapDescription = traps?.toList(),
                loadedRoamingMonsterDescription = monsters?.toList(),
            )
        }
    }

    fun setGeneratedDungeonData(
        dungeon: Array<Array<DungeonTile>>?,
        rooms: List<RoomDescription>?,
        traps: List<TrapDescription>?,
        monsters: List<RoamingMonsterDescription>?
    ) {
        val list = dungeon?.map { row -> row.toList() }
        cachedDungeonList = list
        cachedDungeonArray = dungeon?.map { it.clone() }?.toTypedArray()
        _uiState.update {
            it.copy(
                loadedDungeon = list,
                loadedRoomDescription = rooms?.toList(),
                loadedTrapDescription = traps?.toList(),
                loadedRoamingMonsterDescription = monsters?.toList(),
            )
        }
    }

    fun resetDungeonState() {
        cachedDungeonList = null
        cachedDungeonArray = null
        _uiState.value = DungeonUiState()
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
