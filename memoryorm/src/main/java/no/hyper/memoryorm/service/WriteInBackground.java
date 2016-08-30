package no.hyper.memoryorm.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.Helper.OperationHelper;
import no.hyper.memoryorm.Memory;

/**
 * Created by jean on 30.08.2016.
 */
public class WriteInBackground<T> extends IntentService {

    public static final String CLASS_NAME = "no.hyper.memoryorm.CLASS_NAME";
    public static final String BROADCAST_ACTION = "no.hyper.memoryorm.BROADCAST";
    public static final String ROW_ID = "no.hyper.memoryorm.ROW_ID";
    public static final String IS_LIST = "no.hyper.memoryorm.IS_LIST";
    public static final String WRITE_ACTION = "no.hyper.memoryorm.SAVE_ACTION";
    public static final String ENTITY = "no.hyper.memoryorm.ENTITY";
    public static final String LIST_ENTITY = "no.hyper.memoryorm.LIST_ENTITY";

    public enum Action {
        save, saveList, saveOrUpdate, saveOrUpdateList, update
    }

    private DbManager db;
    private OperationHelper operationHelper;

    private String jsonDb;
    private Action action;
    private T entity;
    private List<T> entities = new ArrayList<>();
    private String className;

    public WriteInBackground() {
        super("WriteInBackground");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        db = DbManager.getInstance(this, getPackageName(), null, 1);
        operationHelper = new OperationHelper(db);

        Bundle extras = intent.getExtras();
        jsonDb = extras.getString(Memory.JSON_DB);
        action = Action.valueOf(extras.getString(WRITE_ACTION));
        entity = extras.getParcelable(ENTITY);
        className = extras.getParcelable(CLASS_NAME);

        Parcelable[] parcelables = extras.getParcelableArray(LIST_ENTITY);
        for( int i = 0; i < parcelables.length; i++) {
            entities.add((T)parcelables[i]);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        db.openDb();

        switch (action) {
            case save: sendResult(save()); break;
            case saveList: sendArrayResult(saveList()); break;
            case saveOrUpdate: sendResult(saveOrUpdate()); break;
            case saveOrUpdateList: sendArrayResult(saveOrUpdateList()); break;
            case update: sendResult(update()); break;
        }

        db.closeDb();
    }

    private Long save() {
        return operationHelper.insert(jsonDb, entity, null);
    }

    private Long[] saveList() {
        List<Long> ids = operationHelper.insert(jsonDb, entities, null);
        Long[] container = new Long[ids.size()];
        return ids.toArray(container);
    }

    private Long saveOrUpdate() {
        return operationHelper.saveOrUpdate(jsonDb, entity);
    }

    private Long[] saveOrUpdateList() {
        List<Long> ids = operationHelper.saveOrUpdate(jsonDb, entities);
        Long[] container = new Long[ids.size()];
        return ids.toArray(container);
    }

    private Long update() {
        return operationHelper.update(jsonDb, entity);
    }

    private void sendResult(Long id) {
        Intent result = new Intent(BROADCAST_ACTION);
        result.putExtra(ROW_ID, id);
        result.putExtra(IS_LIST, false);
        result.putExtra(CLASS_NAME, className);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

    private void sendArrayResult(Long[] ids) {
        Intent result = new Intent(BROADCAST_ACTION);
        result.putExtra(ROW_ID, ids);
        result.putExtra(IS_LIST, true);
        result.putExtra(CLASS_NAME, className);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

}
