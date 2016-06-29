package no.hyper.memoryorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;

/**
 * Created by Jean on 5/15/2016.
 */
public class OperationHelper {

    private DbManager db;

    public OperationHelper(DbManager db) {
        this.db = db;
    }

    /**
     * save an object in the corresponding table. If the object has nested object/list of object, they will be save in
     * their corresponding table.
     * @param entity: the object to save
     * @param foreignKeys: an hash map of foreign keys. The key represent the name of the column, the value is the id
     * @return the id of the row for the object inserted
     */
    public <T, U> long insert(T entity, HashMap<String, Long> foreignKeys) {
        long rowId = -1;
        List<Column> nestedLists = ObjectHelper.getCustomListColumns(entity.getClass().getSimpleName());
        List<Column> nestedObjects = ObjectHelper.getNestedObjects(entity.getClass().getSimpleName());
        ContentValues entityValues = ObjectHelper.getEntityContentValues(entity);

        if (foreignKeys != null) {
            for(Map.Entry<String, Long> key : foreignKeys.entrySet()) {
                entityValues.put(key.getKey(), key.getValue());
            }
        }

        for(Column column : nestedObjects) {
            try {
                Field field = entity.getClass().getDeclaredField(column.getLabel());
                field.setAccessible(true);
                Object actualObject = field.get(entity);
                long id = insert(actualObject, null);
                entityValues.put(column.getLabel(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rowId = db.insert(entity.getClass().getSimpleName(), entityValues);

        for(Column column : nestedLists) {
            try {
                Field field = entity.getClass().getDeclaredField(column.getLabel());
                field.setAccessible(true);
                Object actualObject = field.get(entity);
                HashMap<String, Long> foreignKey = new HashMap<>();
                foreignKey.put("id_" + entity.getClass().getSimpleName(), rowId);
                insertList((List<U>)actualObject, foreignKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rowId;
    }

    /**
     * save a list of object in their corresponding tables
     * @param list: list of object to save
     * @return the list of rows id
     */
    public <T> List<Long> insertList(List<T> list, HashMap<String, Long> foreignKeys) {
        if (list.size() <= 0) return null;
        List<Long> rows = new ArrayList<>();
        for(T entity : list) {
            rows.add(insert(entity, foreignKeys));
        }
        return rows;
    }

    /**
     * fetch all the row of the table
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param condition: WHERE condition, example: "id=3". Can be null
     * @return
     */
    public <T, U> List<T> fetchAll(Class<T> classType, String condition) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), condition));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            T entity = EntityBuilder.bindCursorToEntity(classType, cursor);

            for (Field field : ObjectHelper.getDeclaredFields(classType)) {
                field.setAccessible(true);
                Type listType = ObjectHelper.getActualListType(field);
                if (ObjectHelper.isAList(field) && ObjectHelper.isCustomType(listType.getClass().getSimpleName())) {
                    try {
                        List<U> list = (List<U>)fetchAll(field.getClass(), null);
                        field.setAccessible(true);
                        field.set(entity, list);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (!ObjectHelper.isAList(field) &&
                        ObjectHelper.isCustomType(field.getClass().getSimpleName())) {
                    try {
                        U object = (U)fetchAll(field.getClass(), null);
                        field.setAccessible(true);
                        field.set(entity, object);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            entities.add(entity);
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    public <T> T fetchFirst(Class<T> classType) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), null));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = null;//EntityBuilder.cursorToEntity(classType, cursor, getNestedObjects(classType, cursor));
        cursor.close();
        return entity;
    }

    public <T> T fetchById(Class<T> classType, String id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        //HashMap<String, Object> nestedObjects = getNestedObjects(classType, cursor);
        T entity = null;//EntityBuilder.cursorToEntity(classType, cursor, nestedObjects);
        cursor.close();
        return entity;
    }

    public <T> T fetchByRowId(Class<T> classType, long id) {
        Cursor cursor = proxyRequest(getFetchByRowIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        //HashMap<String, Object> nestedObjects = getNestedObjects(classType, cursor);
        T entity = null;//EntityBuilder.cursorToEntity(classType, cursor, nestedObjects);
        cursor.close();
        return entity;
    }

    public <T> long update(T entity) {
        String id = getEntityId(entity);
        if (id != "-1") {
            return update(entity, id);
        } else {
            return -1;
        }
    }

    public <T> long update(T entity, String id) {
        return db.update(entity.getClass().getSimpleName(), ObjectHelper.getEntityContentValues(entity), id);
    }

    public <T> long saveOrUpdate(T entity) {
        String id = getEntityId(entity);
        Cursor cursor = proxyRequest(getFetchByIdRequest(entity.getClass().getSimpleName(), id));
        if (cursor != null && cursor.getCount() > 0) {
            return update(entity, id);
        } else {
            return insert(entity, null);
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
        String id = "-1";
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName().equals("id")){
                field.setAccessible(true);
                try {
                    id = (String)field.get(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    private <T, U> void insertInRelationTable(T entity, long rowId, Field field) {
        try {
            field.setAccessible(true);
            List<U> items = (List<U>)field.get(entity);
            if (items == null) return;
            for(U item : items) {
                ContentValues values = ObjectHelper.getEntityContentValues(item);
                values.put("id_" + entity.getClass().getSimpleName(), rowId);
                db.insert(item.getClass().getSimpleName(), values);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * return a the list of objects save in db corresponding to the list attribute.
     * @param classType: the class containing the list attribute
     * @param listType: the parametrized type of the list
     * @param rowid: the row id in db of the object containing the list
     */
    public <T, U> List<U> fetchNestedList(Class<T> classType, Class<U> listType, long rowid) {
        String condition = "rowId_" + classType.getSimpleName() + " = " + rowid;
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
        return fetchByRowId(field.getType(), id);
    }

    private String getFetchAllRequest(String table, String condition) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ROWID, * FROM ");
        sb.append(table);
        if (condition != null) {
            sb.append(" WHERE ");
            sb.append(condition);
        }
        sb.append(";");
        return sb.toString();
    }

    private String getFetchByIdRequest(String name, String id) {
        return "SELECT ROWID, * FROM " + name + " WHERE id='" + id + "';";
    }

    private String getFetchByRowIdRequest(String name, long rowId) {
        return "SELECT ROWID, * FROM " + name + " WHERE ROWID='" + rowId + "';";
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
