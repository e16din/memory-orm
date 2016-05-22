package no.hyper.memoryorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
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
        long idEntity = -1;
        List<Field> nestedLists = hasNestedListObjects(entity.getClass());
        List<Field> nestedObjects = hasNestedObjects(entity.getClass());
        ContentValues entityValues = getEntityValues(entity);

        if (nestedObjects.size() > 0) {
            for(Field object : nestedObjects) {
                try {
                    object.setAccessible(true);
                    Object actualObject = object.get(entity);

                    if (actualObject == null) continue;

                    ContentValues objectValues = getEntityValues(actualObject);
                    long id = db.insert(object.getType().getSimpleName(), objectValues);
                    entityValues.put(object.getName(), id);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        idEntity = db.insert(entity.getClass().getSimpleName(), entityValues);

        if (nestedLists.size() > 0) {
            for(Field list : nestedLists) {
                insertInRelationTable(entity, idEntity, list);
            }
        }

        return idEntity;
    }

    public <T> List<Long> insertList(List<T> list) {
        if (list.size() <= 0) return null;
        List<Long> rows = new ArrayList<>();
        for(T entity : list) {
            rows.add(insert(entity));
        }
        return rows;
    }

    public <T> List<T> fetchAll(Class<T> classType, String condition) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), condition));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            HashMap<String, Object> nestedObject = getNestedObjects(classType, cursor);
            entities.add(CursorHelper.cursorToEntity(classType, cursor, nestedObject));
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    public <T> T fetchFirst(Class<T> classType) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), null));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = CursorHelper.cursorToEntity(classType, cursor, getNestedObjects(classType, cursor));
        cursor.close();
        return entity;
    }

    public <T> boolean entityExistInDb(Class<T> classType, long id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor != null && cursor.getCount() >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public <T> T fetchById(Class<T> classType, long id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        HashMap<String, Object> nestedObjects = getNestedObjects(classType, cursor);
        T entity = CursorHelper.cursorToEntity(classType, cursor, nestedObjects);
        cursor.close();
        return entity;
    }

    public <T> int update(T entity) {
        long id = getEntityId(entity);
        return db.update(entity.getClass().getSimpleName(), getEntityValues(entity), String.valueOf(id));
    }

    public <T> long saveOrUpdate(T entity) {
        long id = getEntityId(entity);
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

    private <T> long getEntityId(T entity) {
        long id = 0;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName().equals("id")){
                field.setAccessible(true);
                try {
                    id = field.getLong(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    private <T> List<Field> hasNestedListObjects(Class<T> classType) {
        List<Field> fields = new ArrayList<>();
        for(Field field : classType.getDeclaredFields()) {
            if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                fields.add(field);
            }
        }
        return fields;
    }

    private <T> List<Field> hasNestedObjects(Class<T> classType) {
        List<Field> fields = new ArrayList<>();
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$") || field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                continue;
            } else if (isCustomType(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private <T> ContentValues getEntityValues(T entity) {
        ContentValues values = new ContentValues();
        for(Field field : entity.getClass().getDeclaredFields()) {
            if(field.getName().startsWith("$")) continue;
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(entity);
                if (value == null) {
                    continue;
                } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                    values.put(field.getName(), "1");
                } else {
                    values.put(field.getName(), convertJavaValueToSQLite(value).toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    private <T, U> void insertInRelationTable(T entity, long idEntity, Field field) {
        try {
            field.setAccessible(true);
            List<U> items = (List<U>)field.get(entity);
            if (items == null) return;
            for(U item : items) {
                ContentValues values = getEntityValues(item);
                values.put("id_" + entity.getClass().getSimpleName(), idEntity);
                db.insert(item.getClass().getSimpleName(), values);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <T> HashMap<String, Object> getNestedObjects(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> mapNestedObjects = new HashMap<>();
        List<Field> nestedLists = hasNestedListObjects(classType);
        List<Field> nestedObjects = hasNestedObjects(classType);

        if (nestedLists.size() > 0) {
            for (Field list : nestedLists) {
                int index = cursor.getColumnIndex("id");
                long idEntity = cursor.getLong(index);
                List<Object> relatedList = fetchNestedList(classType, getActualListType(list), idEntity);
                mapNestedObjects.put(list.getName(), relatedList);
            }
        }

        if (nestedObjects.size() > 0) {
            for (Field object : nestedObjects) {
                Object actualObject = fetchNestedObject(object, cursor);
                mapNestedObjects.put(object.getName(), actualObject);
            }
        }

        return mapNestedObjects;
    }

    private <T, U> List<U> fetchNestedList(Class<T> classType, Class<U> listType, long id) {
        List<String> fields = new ArrayList<>();
        for(Field field : listType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                fields.add(listType.getSimpleName() + "." + field.getName());
            } else {
                fields.add(listType.getSimpleName() + "." + field.getName());
            }
        }

        String condition = "id_" + classType.getSimpleName() + " = " + id;
        List<U> items = fetchAll(listType, condition);

        return items;
    }

    private <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    private Object fetchNestedObject(Field field, Cursor cursor) {
        int index = cursor.getColumnIndex(field.getName());
        long id = cursor.getLong(index);
        return fetchById(field.getType(), id);
    }

    private Object convertJavaValueToSQLite(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Boolean" : return ((boolean)value) ? 1 : 0;
            default : return value;
        }
    }

    private String getFetchAllRequest(String table, String condition) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(table);
        if (condition != null) {
            sb.append(" WHERE ");
            sb.append(condition);
        }
        sb.append(";");
        return sb.toString();
    }

    private String getFetchByIdRequest(String name, long id) {
        return "SELECT * FROM " + name + " WHERE id='" + id + "';";
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
