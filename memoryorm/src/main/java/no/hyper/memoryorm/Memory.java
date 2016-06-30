package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean on 5/12/2016.
 */
public class Memory {

    private static final String LOG_TAG = Memory.class.getSimpleName();
    private DbManager db;
    private TableHelper tableHelper;
    private OperationHelper operationHelper;

    public Memory(Context context) {
        db = new DbManager(context, context.getPackageName(), null, 1);
        tableHelper = new TableHelper(db);
        operationHelper = new OperationHelper(db);
    }

    public <T> void createTableFrom(Class<T> classType) {
        db.openDb();
        //tableHelper.createTableFrom(classType);
        db.closeDb();
    }

    public <T> void deleteTable(Class<T> classType) {
        db.openDb();
        //tableHelper.deleteTable(classType);
        db.closeDb();
    }

    public <T> long save(T entity) {
        db.openDb();
        long result = operationHelper.insert(entity, null);
        db.closeDb();
        return result;
    }

    public <T> List<Long> save(List<T> list) {
        if (list.size() <= 0) return null;
        db.openDb();
        List<Long> rows = operationHelper.insertList(list, null);
        db.closeDb();
        return rows;
    }

    public <T> List<T>  fetchAll(Class<T> classType) {
        db.openDb();
        List<T> result = operationHelper.fetchAll(classType, null);
        db.closeDb();
        return result;
    }

    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.openDb();
        T entity = operationHelper.fetchFirst(entityToFetch, null);
        db.closeDb();
        return entity;
    }

    public <T> T fetchById (Class<T> entityToFetch, String id) {
        db.openDb();
        T result = operationHelper.fetchById(entityToFetch, id);
        db.closeDb();
        return result;
    }

    public <T> long update(T entity) {
        db.openDb();
        long result = operationHelper.update(entity);
        db.closeDb();
        return result;
    }

    public <T> List<Long> update(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            ids.add(operationHelper.update(entity));
        }
        db.closeDb();
        return ids;
    }

    public <T> long saveOrUpdate(T entity) {
        db.openDb();
        long result = operationHelper.saveOrUpdate(entity);
        db.closeDb();
        return result;
    }

    public <T> List<Long> saveOrUpdate(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            ids.add(operationHelper.saveOrUpdate(entity));
        }
        db.closeDb();
        return ids;
    }

    public <T> void emptyTable(Class<T> classType) {
        db.openDb();
        //tableHelper.emptyTable(classType);
        db.closeDb();
    }

    public void openDb() {
        this.db.openDb();
    }

    public void closeDb() {
        this.db.closeDb();
    }

    public Cursor rawQuery(String request, String[] args) {
        return db.rawQuery(request, args);
    }

}
