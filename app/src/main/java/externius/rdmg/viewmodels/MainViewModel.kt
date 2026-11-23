package externius.rdmg.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import externius.rdmg.database.DungeonDao
import externius.rdmg.database.DungeonEntity
import externius.rdmg.database.DungeonSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dungeonDao: DungeonDao
) : ViewModel() {

    val dungeonSummaries: StateFlow<List<DungeonSummary>> = dungeonDao.getDungeonSummariesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun deleteDungeon(id: Long) {
        viewModelScope.launch {
            dungeonDao.deleteById(id)
        }
    }

    fun deleteAllDungeons() {
        viewModelScope.launch {
            dungeonDao.deleteAll()
        }
    }

    suspend fun getDungeonById(id: Long): DungeonEntity? = dungeonDao.getDungeonById(id)
}
