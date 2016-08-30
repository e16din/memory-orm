package no.hyper.memoryorm.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.Helper.ObjectHelper;
import no.hyper.memoryorm.Helper.OperationHelper;
import no.hyper.memoryorm.Memory;
/**
 * Created by jean on 25.08.2016.
 */
public class FetchInBackground <T> extends IntentService {

    public static final String CLASS_NAME = "no.hyper.memoryorm.CLASS_NAME";
    public static final String BROADCAST_ACTION = "no.hyper.memoryorm.BROADCAST";
    public static final String PARCELABLE = "no.hyper.memoryorm.PARCELABLE";
    public static final String FETCH_ACTION = "no.hyper.memoryorm.FETCH_ACTION";
    public static final String ID = "no.hyper.memoryorm.ID";
    public static final String ROW_ID = "no.hyper.memoryorm.ROW_ID";
    public static final String CONDITION = "no.hyper.memoryorm.CONDITION";
    public static final String IS_LIST = "no.hyper.memoryorm.IS_LISt";

    public enum Action {
        fetchFirst, fetchById, fetchByRowId, fetchAll
    }

    private String jsonDb;
    private String className;
    private Action action;
    private String id;
    private Long rowId;
    private String condition;

    public FetchInBackground() {
        super("FetchInBackground");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();
        jsonDb = extras.getString(Memory.JSON_DB);
        className = extras.getString(CLASS_NAME);
        action = Action.valueOf(extras.getString(FETCH_ACTION));
        id = extras.getString(ID);
        rowId = extras.getLong(ROW_ID);
        condition = extras.getString(CONDITION);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DbManager db = DbManager.getInstance(this, getPackageName(), null, 1);
        OperationHelper operationHelper = new OperationHelper(db);
        Class<T> classType = ObjectHelper.getClassFromName(className);
        db.openDb();

        switch (action) {
            case fetchFirst: sendResult(fetchFirst(operationHelper, jsonDb, classType, condition)); break;
            case fetchById: sendResult(fetchById(operationHelper, jsonDb, classType, id)); break;
            case fetchByRowId: sendResult(fetchByRowId(operationHelper, jsonDb, classType, rowId)); break;
            case fetchAll: sendArrayResult(fetchAll(operationHelper, jsonDb, classType, condition)); break;
        }

        db.closeDb();
    }

    private Parcelable fetchFirst(OperationHelper operationHelper, String jsonDb, Class<T> classType, String condition) {
        return (Parcelable)operationHelper.fetchFirst(jsonDb, classType, condition);
    }

    private Parcelable fetchById(OperationHelper operationHelper, String jsonDb, Class<T> classType, String id) {
        return (Parcelable)operationHelper.fetchById(jsonDb, classType, id);
    }

    private Parcelable fetchByRowId(OperationHelper operationHelper, String jsonDb, Class<T> classType, Long rowId) {
        return (Parcelable)operationHelper.fetchByRowId(jsonDb, classType, rowId);
    }

    private Parcelable[] fetchAll(OperationHelper operationHelper, String jsonDb, Class<T> classType, String condition) {
        List<T> result = operationHelper.fetchAll(jsonDb, classType, condition);
        Parcelable[] parcelables = new Parcelable[result.size()];
        for(int i = 0; i < parcelables.length; i++) {
            parcelables[i] = (Parcelable)result.get(i);
        }
        return parcelables;
    }

    private void sendResult(Parcelable parcelable) {
        Intent result = new Intent(BROADCAST_ACTION);
        result.putExtra(PARCELABLE, parcelable);
        result.putExtra(CLASS_NAME, className);
        result.putExtra(IS_LIST, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

    private void sendArrayResult(Parcelable[] parcelables) {
        Intent result = new Intent(BROADCAST_ACTION);
        result.putExtra(PARCELABLE, parcelables);
        result.putExtra(CLASS_NAME, className);
        result.putExtra(IS_LIST, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

}
