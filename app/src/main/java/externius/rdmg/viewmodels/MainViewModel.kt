package externius.rdmg.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import externius.rdmg.database.DungeonSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dungeonDao = AppDatabase.getInstance(application).dungeonDao()

    val dungeonSummaries: StateFlow<List<DungeonSummary>> = dungeonDao.getDungeonSummariesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun deleteDungeon(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dungeonDao.deleteById(id)
        }
    }

    fun deleteAllDungeons() {
        viewModelScope.launch(Dispatchers.IO) {
            dungeonDao.deleteAll()
        }
    }

    suspend fun getDungeonById(id: Long): DungeonEntity? = withContext(Dispatchers.IO) {
        dungeonDao.getDungeonById(id)
    }
}
