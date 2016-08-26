package no.hyper.memoryorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Jean on 5/12/2016.
 */
public class DbManager extends SQLiteOpenHelper {

    private final static String LOG_TAG = DbManager.class.getSimpleName();
    private SQLiteDatabase db;
    private static DbManager instance;

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "$LOG_TAG onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private DbManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
    }

    public static DbManager getInstance(Context context, String dbName, SQLiteDatabase.CursorFactory factory,
                                        int version) {
        if (instance == null) {
            instance = new DbManager(context, dbName, factory, version);
        }
        return instance;
    }

    public void openDb() {
        if (db == null || !db.isOpen()) {
            try {
                while (db.isDbLockedByCurrentThread())
                db = this.getWritableDatabase();
            } catch (NullPointerException e) {}
        }
    }

    public boolean isDbOpen() {
        return !(db == null || !db.isOpen()) && db.isOpen();
    }

    public void closeDb() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public Cursor rawQuery(String request, String[] args) {
        if (db != null && db.isOpen()) {
            return db.rawQuery(request, args);
        } else {
            return null;
        }
    }

    public void execute(String request) {
        if (db != null && db.isOpen()) {
            db.execSQL(request);
        }
    }

    public long insert(String tableName, ContentValues values) {
        try {
            if (db != null && db.isOpen()) {
                return db.insert(tableName, null, values);
            } else {
                return 0;
            }
        } catch (IllegalStateException error) {
            return -1;
        }
    }

    public int update(String tableName, ContentValues values, String id) {
        if (db != null && db.isOpen()) {
            return db.update(tableName, values, "id='" + id + "'", null);
        } else {
            return 0;
        }
    }

    public int delete(String tableName, String clause) {
        if (db != null && db.isOpen()) {
            return db.delete(tableName, clause, null);
        } else {
            return 0;
        }
    }

}

