package externius.rdmg.database;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DungeonsProvider extends ContentProvider {
    private static final String AUTHORITY = "externius.rdmg.dungeonsprovider";
    private static final String BASE_PATH = "dungeons";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final int DUNGEONS = 1;
    private static final int DUNGEONS_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, DUNGEONS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", DUNGEONS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        if (uriMatcher.match(uri) == DUNGEONS_ID) {
            s = DBOpenHelper.DUNGEON_ID + "=" + uri.getLastPathSegment();
        }
        return database.query(DBOpenHelper.TABLE_DUNGEONS, DBOpenHelper.ALL_COLUMNS, s, null, null, null, DBOpenHelper.DUNGEON_CREATED + " DESC");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = database.insertOrThrow(DBOpenHelper.TABLE_DUNGEONS, null, contentValues);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return database.delete(DBOpenHelper.TABLE_DUNGEONS, s, strings);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return database.update(DBOpenHelper.TABLE_DUNGEONS, contentValues, s, strings);
    }
}
