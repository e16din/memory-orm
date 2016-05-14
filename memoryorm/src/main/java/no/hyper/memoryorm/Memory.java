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
    DbManager db;

    /*public <T> List<T>  fetchAllWithNesteedObject(Class<T> entityToFetch, List<String> nestedAttributes) {
        return db.fetchAllWithNestedObject(entityToFetch, nestedAttributes);
    }

    public <T> T fetchById (Class<T> entityToFetch, String id) {
        return db.fetchById(entityToFetch, id);
    }

    public <T> void updateOrInsert(T entity) {
        db.updateOrInsertEntity(entity);
    }

    public <T> void updateOrInsertList(List<T> list) {
        db.updateOrInsertList(list);
    }

    public <T> void updateOrInsertListWithNestedObject(List<T> list, HashMap<String, Type> nestedType) {
        db.updateOrInsertListWithNestedObject(list, nestedType);
    }*/

    // -------------------------------------------------------------------------------------
    // REFACTORING ->

    public Memory(Context context) {
        db = new DbManager(context, context.getPackageName(), null, 1);
    }

    public <T> int createTableFrom(Class<T> classType) {
        db.open();
        int result = db.createTableFrom(classType, false);
        db.close();
        return result;
    }

    public <T> int createTableFrom(Class<T> classType, boolean autoincrement) {
        db.open();
        int result = db.createTableFrom(classType, autoincrement);
        db.close();
        return result;
    }

    public <T> int deleteTable(Class<T> classType) {
        db.open();
        int result = db.deleteTable(classType);
        db.close();
        return result;
    }

    public <T> Long save(T entity) {
        db.open();
        int execute = db.createTableFrom(entity.getClass(), false);
        if (execute != -1) {
            long result = db.save(entity);
            db.close();
            return result;
        } else {
            db.close();
            return (long)execute;
        }
    }

    public <T> List<Long> save(List<T> list) {
        db.open();
        List<Long> rows = db.saveList(list);
        db.close();
        return rows;
    }

    public <T> List<T>  fetchAll(Class<T> classType) {
        db.open();
        List<T> result = db.fetchAll(classType);
        db.close();
        return result;
    }

    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.open();
        List<T> list = db.fetchAll(entityToFetch);
        db.close();
        if(list != null ) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
