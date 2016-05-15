package no.hyper.memoryorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean on 5/15/2016.
 */
public class OperationHelper {

    private DbManager db;

    public OperationHelper(DbManager db) {
        this.db = db;
    }

    public <T> long insert(T entity) {
        ContentValues values = new ContentValues();
        for(Field field : entity.getClass().getDeclaredFields()) {
            if(field.getName().startsWith("$")) continue;
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(entity);
                if (value == null) continue;
                values.put(field.getName(), convertJavaValueToSQLite(value).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return db.insert(entity.getClass().getSimpleName(), values);
    }

    public <T> List<Long> insertList(List<T> list) {
        if (list.size() <= 0) return null;
        List<Long> rows = new ArrayList<>();
        for(T entity : list) {
            rows.add(insert(entity));
        }
        return rows;
    }

    public <T> List<T> fetchAll(Class<T> classType) {
        Cursor cursor = db.rawQuery(getfetchAllRequest(classType.getSimpleName()), null);
        if (cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            entities.add(CursorHelper.cursorToEntity(classType, cursor));
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    public <T> T fetchById(Class<T> classType, String id) {
        Cursor cursor = db.rawQuery(getFetchByIdRequest(classType.getSimpleName(), id), null);
        if (cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = CursorHelper.cursorToEntity(classType, cursor);
        cursor.close();
        return entity;
    }

    private Object convertJavaValueToSQLite(Object value) {
        if (value.toString().equals("true")) {
            return 1;
        } else if (value.toString().equals("false")) {
            return 0;
        } else {
            return value;
        }
    }

    private String getfetchAllRequest(String name) {
        return "SELECT * FROM " + name + ";";
    }

    public String getFetchByIdRequest(String name, String id) {
        return "SELECT * FROM " + name + " WHERE ID='" + id + "';";
    }

}
