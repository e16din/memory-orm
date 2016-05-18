package no.hyper.memoryorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jean on 5/15/2016.
 */
public class OperationHelper {

    private DbManager db;

    public OperationHelper(DbManager db) {
        this.db = db;
    }

    public <T> long insert(T entity) {
        ContentValues values = getEntityValues(entity);
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
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName()));
        if (cursor == null || cursor.getCount() > 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            entities.add(CursorHelper.cursorToEntity(classType, cursor, getNestedObjects(classType, cursor)));
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    public <T> T fetchFirst(Class<T> classType) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName()));
        if (cursor == null || cursor.getCount() > 0) return null;

        cursor.moveToFirst();
        T entity = CursorHelper.cursorToEntity(classType, cursor, getNestedObjects(classType, cursor));
        cursor.close();
        return entity;
    }

    public <T> boolean entityExistInDb(Class<T> classType, String id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor != null && cursor.getCount() >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public <T> T fetchById(Class<T> classType, String id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() > 0) return null;

        cursor.moveToFirst();
        T entity = CursorHelper.cursorToEntity(classType, cursor, getNestedObjects(classType, cursor));
        cursor.close();
        return entity;
    }

    public <T> int update(T entity) {
        String id = getEntityId(entity);
        return db.update(entity.getClass().getSimpleName(), getEntityValues(entity), String.valueOf(id));
    }

    public <T> long saveOrUpdate(T entity) {
        String id = getEntityId(entity);
        if (entityExistInDb(entity.getClass(), id)) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    private Cursor proxyRequest(String request) {
        try {
            return db.rawQuery(request, null);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> String getEntityId(T entity) {
        String id = "0";
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName().equals("id")){
                field.setAccessible(true);
                try {
                    id = (String)field.get(entity);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    private <T> ContentValues getEntityValues(T entity) {
        ContentValues values = new ContentValues();
        for(Field field : entity.getClass().getDeclaredFields()) {
            if(field.getName().startsWith("$")) continue;
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(entity);
                if (value == null) continue;
                if (isCustomType(field)) {
                    long id = insert(value);
                    values.put(field.getName(), String.valueOf(id));
                } else {
                    values.put(field.getName(), convertJavaValueToSQLite(value).toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    private <T> HashMap<String, Object> getNestedObjects(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> nestedObjects = new HashMap<>();
        for(Field field : classType.getDeclaredFields()) {
            if (isCustomType(field)) {
                nestedObjects.put(field.getName(), fetchNestedObject(field, cursor));
            }
        }
        return  nestedObjects;
    }

    private Object fetchNestedObject(Field field, Cursor cursor) {
        int index = cursor.getColumnIndex(field.getName());
        long id = cursor.getLong(index);
        return fetchById(field.getType(), String.valueOf(id));
    }

    private Object convertJavaValueToSQLite(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Boolean" : return ((boolean)value) ? 1 : 0;
            default : return value;
        }
    }

    private String getFetchAllRequest(String name) {
        return "SELECT * FROM " + name + ";";
    }

    private String getFetchByIdRequest(String name, String id) {
        return "SELECT * FROM " + name + " WHERE id='" + id + "';";
    }

    private String getUpdateRequest(String tableName, String values, String id) {
        return "UPDATE " + tableName  + " SET " + values  + " WHERE id=" + id + ";";
    }

    private boolean isCustomType(Field field) {
        try {
            if (field.getType().isPrimitive()) {
                return false;
            }
            Class.forName("java.lang." + field.getType().getSimpleName());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

}
