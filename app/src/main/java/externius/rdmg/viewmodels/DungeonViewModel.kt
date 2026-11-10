package externius.rdmg.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import externius.rdmg.database.AppDatabase
import externius.rdmg.database.DungeonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DungeonViewModel(application: Application) : AndroidViewModel(application) {

    private val dungeonDao = AppDatabase.getInstance(application).dungeonDao()

    suspend fun getDungeonById(id: Long): DungeonEntity? = withContext(Dispatchers.IO) {
        dungeonDao.getDungeonById(id)
    }

    suspend fun updateDungeon(entity: DungeonEntity): Int = withContext(Dispatchers.IO) {
        dungeonDao.update(entity)
    }

    suspend fun insertDungeon(entity: DungeonEntity): Long = withContext(Dispatchers.IO) {
        dungeonDao.insert(entity)
    }
}
