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

    public Memory(Context context) {
        db = new DbManager(context, context.getPackageName(), null, 1);
    }

    public <T> void saveList(List<T> list) {
        db.saveList(list);
    }

    public <T> Long save(T entityToSave) {
        return db.save(entityToSave);
    }

    public <T> List<T>  fetchAllWithNesteedObject(Class<T> entityToFetch, List<String> nestedAttributes) {
        return db.fetchAllWithNestedObject(entityToFetch, nestedAttributes);
    }

    public <T> List<T>  fetchAll(Class<T> entityToFetch) {
        return db.fetchAll(entityToFetch);
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

    public <T> void deleteTable(Class<T> entityToDelete) {
        db.deleteTable(entityToDelete.getSimpleName());
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

    public <T> void createTableFrom(Class<T> entity) {
        db.createTableFrom(entity);
    }

}
