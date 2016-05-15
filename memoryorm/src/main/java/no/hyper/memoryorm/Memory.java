package no.hyper.memoryorm;

import android.content.Context;

import java.lang.reflect.Type;
import java.util.HashMap;
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

    public <T> int createTableFrom(Class<T> classType) {
        db.open();
        int result = tableHelper.createTableFrom(classType, false);
        db.close();
        return result;
    }

    public <T> int createTableFrom(Class<T> classType, boolean autoincrement) {
        db.open();
        int result = tableHelper.createTableFrom(classType, autoincrement);
        db.close();
        return result;
    }

    public <T> int deleteTable(Class<T> classType) {
        db.open();
        int result = tableHelper.deleteTable(classType);
        db.close();
        return result;
    }

    public <T> long save(T entity) {
        int execute = tableHelper.createTableFrom(entity.getClass(), false);
        if (execute != DbManager.EXECUTE_FAIL) {
            db.open();
            long result = operationHelper.insert(entity);
            db.close();
            return result;
        } else {
            return (long)execute;
        }
    }

    public <T> List<Long> save(List<T> list) {
        db.open();
        List<Long> rows = operationHelper.insertList(list);
        db.close();
        return rows;
    }

    public <T> List<T>  fetchAll(Class<T> classType) {
        db.open();
        List<T> result = operationHelper.fetchAll(classType);
        db.close();
        return result;
    }

    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.open();
        List<T> list = operationHelper.fetchAll(entityToFetch);
        db.close();
        if (list != null) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public <T> T fetchById (Class<T> entityToFetch, String id) {
        db.open();
        T result = operationHelper.fetchById(entityToFetch, id);
        db.close();
        return result;
    }
}
