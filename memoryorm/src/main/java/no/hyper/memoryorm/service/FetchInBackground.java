package no.hyper.memoryorm.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

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

    private String jsonDb;
    private String className;

    public FetchInBackground() {
        super("FetchInBackground");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();
        jsonDb = extras.getString(Memory.JSON_DB);
        className = extras.getString(CLASS_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DbManager db = DbManager.getInstance(this, getPackageName(), null, 1);
        OperationHelper operationHelper = new OperationHelper(db);
        db.openDb();
        Class<T> classType = ObjectHelper.getClassFromName(className);
        T entity = operationHelper.fetchFirst(jsonDb, classType, null);
        db.closeDb();

        Intent result = new Intent(BROADCAST_ACTION);
        result.putExtra(PARCELABLE, (Parcelable)entity);
        result.putExtra(CLASS_NAME, className);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

}
