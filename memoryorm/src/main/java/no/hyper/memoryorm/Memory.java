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

    public <T> void saveList(List<T> list) {
        db.saveList(list);
    }

    public <T> List<T>  fetchAllWithNesteedObject(Class<T> entityToFetch, List<String> nestedAttributes) {
        return db.fetchAllWithNestedObject(entityToFetch, nestedAttributes);
    }

    public <T> T fetchFirst(Class<T> entityToFetch) {
        List<T> list = db.fetchAll(entityToFetch);
        if(list != null ) {
            return list.get(0);
        } else {
            return null;
        }
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
    }

    // -------------------------------------------------------------------------------------
    // REFACTORING ->

    public static final int SUCCESS_EXECUTE = 1;
    public static final long SUCCESS_INSERT = 1;
    public static final int FAIL_EXECUTE = -1;

    public static final int TABLE_ALREADY_EXIST = 10;


    public Memory(Context context) {
        db = new DbManager(context, context.getPackageName(), null, 1);
    }

    public <T> int createTableFrom(Class<T> classType) {
        return db.createTableFrom(classType, false);
    }

    public <T> int createTableFrom(Class<T> classType, boolean autoincrement) {
        return db.createTableFrom(classType, autoincrement);
    }

    public <T> int deleteTable(Class<T> classType) {
        return db.deleteTable(classType);
    }

    public <T> Long save(T entity) {
        int result = createTableFrom(entity.getClass());
        if (result != FAIL_EXECUTE) {
            return db.save(entity);
        } else {
            return (long)result;
        }
    }

    public <T> List<T>  fetchAll(Class<T> classType) {
        return db.fetchAll(classType);
    }
}
