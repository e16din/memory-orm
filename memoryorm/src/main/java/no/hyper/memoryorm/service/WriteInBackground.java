package no.hyper.memoryorm.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.helper.OperationHelper;
import no.hyper.memoryorm.helper.SchemaHelper;

/**
 * Created by jean on 30.08.2016.
 */
public class WriteInBackground extends IntentService {

    private final String LOG_TAG = this.className;

    public static final String CLASS_NAME = "no.hyper.memoryorm.CLASS_NAME";
    public static final String BROADCAST_ACTION_WRITE = "no.hyper.memoryorm.BROADCAST_WRITE";
    public static final String ROW_ID = "no.hyper.memoryorm.ROW_ID";
    public static final String IS_LIST = "no.hyper.memoryorm.IS_LIST";
    public static final String WRITE_ACTION = "no.hyper.memoryorm.WRITE_ACTION";
    public static final String ENTITY = "no.hyper.memoryorm.ENTITY";
    public static final String LIST_ENTITY = "no.hyper.memoryorm.LIST_ENTITY";

    public enum Action {
        save, saveList, saveOrUpdate, saveOrUpdateList, update
    }

    private DbManager db;
    private OperationHelper operationHelper;

    private String jsonDb;
    private Action action;
    private Parcelable entity;
    private List<Parcelable> entities = new ArrayList<>();
    private String className;

    public WriteInBackground() {
        super("WriteInBackground");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        db = DbManager.getInstance(this, getPackageName(), null, 1);
        operationHelper = new OperationHelper(db);

        try {
            Bundle extras = intent.getExtras();
            jsonDb = SchemaHelper.getInstance().getDatabase(this);
            className = extras.getString(CLASS_NAME);
            action = Action.valueOf(extras.getString(WRITE_ACTION));
            entity = extras.getParcelable(ENTITY);

            Parcelable[] parcelables = extras.getParcelableArray(LIST_ENTITY);
            if (parcelables != null) {
                for( int i = 0; i < parcelables.length; i++) {
                    entities.add(parcelables[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (entity == null) return Long.valueOf(0);
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
        Intent result = new Intent(BROADCAST_ACTION_WRITE);
        result.putExtra(ROW_ID, id);
        result.putExtra(IS_LIST, false);
        result.putExtra(CLASS_NAME, className);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

    private void sendArrayResult(Long[] ids) {
        Intent result = new Intent(BROADCAST_ACTION_WRITE);
        result.putExtra(ROW_ID, ids);
        result.putExtra(IS_LIST, true);
        result.putExtra(CLASS_NAME, className);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

}
