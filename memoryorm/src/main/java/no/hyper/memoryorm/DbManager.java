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

    public static final  int EXECUTE_SUCCESS = 1;
    public static final int EXECUTE_FAIL = -1;

    private final static String LOG_TAG = DbManager.class.getSimpleName();
    private SQLiteDatabase db;

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

    public DbManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            db = this.getWritableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public Cursor rawQuery(String request, String[] args) {
        return db.rawQuery(request, args);
    }

    public int execute(String request) {
        try {
            db.execSQL(request);
            return EXECUTE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EXECUTE_FAIL;
        }
    }

    public long insert(String tableName, ContentValues values) {
        return db.insert(tableName, null, values);
    }

}

