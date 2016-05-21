package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

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
        db.open();
        tableHelper.createTableIfNecessaryFrom(classType, false);
        db.close();
    }

    public <T> void createTableFrom(Class<T> classType, boolean autoincrement) {
        db.open();
        tableHelper.createTableIfNecessaryFrom(classType, autoincrement);
        db.close();
    }

    public <T> void deleteTable(Class<T> classType) {
        db.open();
        tableHelper.deleteTable(classType);
        db.close();
    }

    public <T> long save(T entity) {
        db.open();
        tableHelper.createTableIfNecessaryFrom(entity.getClass(), false);
        long result = operationHelper.insert(entity);
        db.close();
        return result;
    }

    public <T> List<Long> save(List<T> list) {
        if (list.size() <= 0) return null;
        db.open();
        tableHelper.createTableIfNecessaryFrom(list.get(0).getClass(), false);
        List<Long> rows = operationHelper.insertList(list);
        db.close();
        return rows;
    }

    public <T> List<T>  fetchAll(Class<T> classType) {
        db.open();
        List<T> result = operationHelper.fetchAll(classType, null);
        db.close();
        return result;
    }

    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.open();
        T entity = operationHelper.fetchFirst(entityToFetch);
        db.close();
        return entity;
    }

    public <T> T fetchById (Class<T> entityToFetch, long id) {
        db.open();
        T result = operationHelper.fetchById(entityToFetch, id);
        db.close();
        return result;
    }

    public <T> int update(T entity) {
        db.open();
        int result = operationHelper.update(entity);
        db.close();
        return result;
    }

    public <T> long saveOrUpdate(T entity) {
        db.open();
        long result = operationHelper.saveOrUpdate(entity);
        db.close();
        return result;
    }

    public void openDb() {
        this.db.open();
    }

    public void closeDb() {
        this.db.close();
    }

    public Cursor rawQuery(String request, String[] args) {
        return db.rawQuery(request, args);
    }
}
