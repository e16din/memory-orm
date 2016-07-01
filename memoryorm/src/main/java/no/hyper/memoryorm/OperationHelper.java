package no.hyper.memoryorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hyper.memoryorm.model.Column;

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
                insert((List<U>)actualObject, foreignKey);
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
    public <T> List<Long> insert(List<T> list, HashMap<String, Long> foreignKeys) {
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
     */
    public <T> List<T> fetchAll(Class<T> classType, String condition) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), condition));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            T entity = getEntity(classType, cursor);
            entities.add(entity);
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    /**
     * fetch all the row of the table and return the first
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param condition: WHERE condition, example: "id=3". Can be null
     */
    public <T> T fetchFirst(Class<T> classType, String condition) {
        Cursor cursor = proxyRequest(getFetchAllRequest(classType.getSimpleName(), condition));
        if (cursor == null || cursor.getCount() <= 0) return null;
        cursor.moveToFirst();
        T entity = getEntity(classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * fetch the row of the table that has the corresponding ID than the one provided
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param id: the id to look for
     */
    public <T> T fetchById(Class<T> classType, String id) {
        Cursor cursor = proxyRequest(getFetchByIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = getEntity(classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * fetch the row of the table that is at the corresponding row id than the one provided
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param id: the row id to look for
     * <p>The row id or ROWID is an hidden field generated by Sqlite for each row of every table. It correspond to
     * a long value.</p>
     */
    public <T> T fetchByRowId(Class<T> classType, long id) {
        Cursor cursor = proxyRequest(getFetchByRowIdRequest(classType.getSimpleName(), id));
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = getEntity(classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * update the row corresponding to the entity passed by argument
     * @return true if it worked otherwise false
     */
    public <T> boolean update(T entity) {
        String id = getEntityId(entity);
        if (id != "-1") {
            long number = update(entity, id);
            return number > 0;
        } else {
            return false;
        }
    }

    /**
     * Save the entity in db if it does not exist or update it otherwise.
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted
     */
    public <T> long saveOrUpdate(T entity) {
        String id = getEntityId(entity);
        if (!id.equals("-1")) {
            Cursor cursor = proxyRequest(getFetchByIdRequest(entity.getClass().getSimpleName(), id));
            if (cursor != null && cursor.getCount() > 0) {
                update(entity, id);
                return 0;
            } else {
                return insert(entity, null);
            }
        } else {
            return -1;
        }
    }

    /**
     * for each item in the list, it save it in db if it does not exist, or update it otherwise.
     * @return for each items, -1 if it failed, 0 if a row was updated or the rowid if it inserted
     */
    public <T> List<Long> saveOrUpdate(List<T> list) {
        if (list.size() <= 0) return null;
        List<Long> results = new ArrayList<>();
        for(T entity : list) {
            results.add(saveOrUpdate(entity));
        }
        return results;
    }

    private Cursor proxyRequest(String request) {
        try {
            return db.rawQuery(request, null);
        } catch (Exception e) {
            return null;
        }
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

    private <T, U> T getEntity(Class<T> classType, Cursor cursor) {
        T entity = EntityBuilder.bindCursorToEntity(classType, cursor);
        for (Field field : ObjectHelper.getDeclaredFields(classType)) {
            if (ObjectHelper.isAList(field)) {
                Type listType = ObjectHelper.getActualListType(field);
                if (ObjectHelper.isCustomType(listType.getClass().getSimpleName())) {
                    try {
                        Field idField = classType.getDeclaredField("id");
                        String id = (String)idField.get(entity);
                        List<U> list = (List<U>)fetchAll(field.getClass(), "id_" + classType.getSimpleName() + "=" + id);
                        field.setAccessible(true);
                        field.set(entity, list);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            } else if (!ObjectHelper.isAList(field) &&
                    ObjectHelper.isCustomType(field.getType().getSimpleName())) {
                try {
                    int idx = cursor.getColumnIndex(field.getName());
                    long rowId = cursor.getLong(idx);
                    U object = (U)fetchFirst(field.getType(), "ROWID="+rowId);
                    field.setAccessible(true);
                    field.set(entity, object);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return entity;
    }

    private <T> long update(T entity, String id) {
        return db.update(entity.getClass().getSimpleName(), ObjectHelper.getEntityContentValues(entity), id);
    }

    private <T> String getEntityId(T entity) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return (String)field.get(entity);
        } catch (NoSuchFieldException e) {
            return "-1";
        } catch (IllegalAccessException e) {
            return "-1";
        }
    }

}
