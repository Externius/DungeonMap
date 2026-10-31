package externius.rdmg.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.net.toUri

class DungeonsProvider : ContentProvider() {

    private var database: SQLiteDatabase? = null

    override fun onCreate(): Boolean {
        val helper = DBOpenHelper(context)
        database = helper.writableDatabase
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        var sel = selection
        if (uriMatcher.match(uri) == DUNGEONS_ID) {
            sel = "${DBOpenHelper.DUNGEON_ID}=${uri.lastPathSegment}"
        }
        return database?.query(
            DBOpenHelper.TABLE_DUNGEONS,
            DBOpenHelper.ALL_COLUMNS,
            sel,
            null,
            null,
            null,
            "${DBOpenHelper.DUNGEON_CREATED} DESC"
        )
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = database ?: return null
        val id = db.insertOrThrow(DBOpenHelper.TABLE_DUNGEONS, null, values)
        return "$BASE_PATH/$id".toUri()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return database?.delete(DBOpenHelper.TABLE_DUNGEONS, selection, selectionArgs) ?: 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return database?.update(DBOpenHelper.TABLE_DUNGEONS, values, selection, selectionArgs) ?: 0
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
