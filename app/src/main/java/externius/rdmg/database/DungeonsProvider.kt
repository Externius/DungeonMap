package externius.rdmg.database

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.net.toUri
import androidx.sqlite.db.SupportSQLiteQueryBuilder

class DungeonsProvider : ContentProvider() {

    private lateinit var database: AppDatabase

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        database = AppDatabase.getInstance(ctx)
        return true
    }

    private fun getSelectionWithId(uri: Uri, selection: String?): String? {
        return if (uriMatcher.match(uri) == DUNGEONS_ID) {
            val idWhere = "${DBOpenHelper.DUNGEON_ID} = ${uri.lastPathSegment}"
            if (selection.isNullOrEmpty()) idWhere else "($idWhere) AND ($selection)"
        } else {
            selection
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val finalSelection = getSelectionWithId(uri, selection)
        val order = if (sortOrder.isNullOrEmpty()) {
            "${DBOpenHelper.DUNGEON_CREATED} DESC, ${DBOpenHelper.DUNGEON_ID} DESC"
        } else {
            sortOrder
        }
        val query = SupportSQLiteQueryBuilder
            .builder(DBOpenHelper.TABLE_DUNGEONS)
            .columns(projection)
            .selection(finalSelection, selectionArgs)
            .orderBy(order)
            .create()

        val cursor = database.openHelper.writableDatabase.query(query)
        context?.let { ctx ->
            cursor.setNotificationUri(ctx.contentResolver, uri)
        }
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        val createdTime = values.getAsString(DBOpenHelper.DUNGEON_CREATED)
            ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())
        val entity = DungeonEntity(
            dungeonName = values.getAsString(DBOpenHelper.DUNGEON_NAME),
            loadedDungeon = values.getAsString(DBOpenHelper.LOADED_DUNGEON) ?: "",
            loadedRoomDescription = values.getAsString(DBOpenHelper.LOADED_ROOM_DESCRIPTION),
            loadedTrapDescription = values.getAsString(DBOpenHelper.LOADED_TRAP_DESCRIPTION),
            loadedRoamingMonsters = values.getAsString(DBOpenHelper.LOADED_ROAMING_MONSTERS),
            dungeonDifficulty = values.getAsString(DBOpenHelper.DUNGEON_DIFFICULTY),
            partyLevel = values.getAsString(DBOpenHelper.PARTY_LEVEL),
            partySize = values.getAsString(DBOpenHelper.PARTY_SIZE),
            treasureValue = values.getAsString(DBOpenHelper.TREASURE_VALUE),
            itemsRarity = values.getAsString(DBOpenHelper.ITEMS_RARITY),
            dungeonSize = values.getAsString(DBOpenHelper.DUNGEON_SIZE),
            roomDensity = values.getAsString(DBOpenHelper.ROOM_DENSITY),
            roomSize = values.getAsString(DBOpenHelper.ROOM_SIZE),
            traps = values.getAsString(DBOpenHelper.TRAPS),
            corridors = values.getAsString(DBOpenHelper.CORRIDORS),
            monsterType = values.getAsString(DBOpenHelper.MONSTER_TYPE),
            deadEnds = values.getAsString(DBOpenHelper.DEAD_ENDS),
            roamingMonsters = values.getAsString(DBOpenHelper.ROAMING_MONSTERS),
            dungeonCreated = createdTime
        )
        val id = database.dungeonDao().insert(entity)
        if (id > 0) {
            val insertedUri = ContentUris.withAppendedId(CONTENT_URI, id)
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
            return insertedUri
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val finalSelection = getSelectionWithId(uri, selection)
        val db = database.openHelper.writableDatabase
        val count = db.delete(DBOpenHelper.TABLE_DUNGEONS, finalSelection, selectionArgs)
        if (count > 0) {
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
        }
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (values == null || values.size() == 0) return 0
        val finalSelection = getSelectionWithId(uri, selection)
        val db = database.openHelper.writableDatabase
        val count = db.update(
            DBOpenHelper.TABLE_DUNGEONS,
            SQLiteDatabase.CONFLICT_NONE,
            values,
            finalSelection,
            selectionArgs
        )
        if (count > 0) {
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
        }
        return count
    }

    companion object {
        private const val AUTHORITY = "externius.rdmg.dungeonsprovider"
        private const val BASE_PATH = "dungeons"

        @JvmField
        val CONTENT_URI: Uri = "content://$AUTHORITY/$BASE_PATH".toUri()

        private const val DUNGEONS = 1
        private const val DUNGEONS_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, BASE_PATH, DUNGEONS)
            addURI(AUTHORITY, "$BASE_PATH/#", DUNGEONS_ID)
        }
    }
}
