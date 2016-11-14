package no.hyper.memoryorm.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.builder.EntityBuilder;
import no.hyper.memoryorm.builder.QueryBuilder;
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
     * @param context the android context
     * @param entity: the object to save
     * @param foreignKeys: an hash map of foreign keys. The key represent the name of the column, the value is the id
     * @return the id of the row for the object inserted
     */
    public <T, U> long insert(Context context, T entity, HashMap<String, Long> foreignKeys) throws IOException {
        List<Column> nestedLists = ObjectHelper.getCustomListColumns(context, entity.getClass().getSimpleName());
        List<Column> nestedObjects = ObjectHelper.getNestedObjects(context, entity.getClass().getSimpleName());
        ContentValues entityValues = ObjectHelper.getEntityContentValues(context, entity);

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
                long id = insert(context, actualObject, null);
                entityValues.put(column.getLabel(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Long rowId = db.insert(entity.getClass().getSimpleName(), entityValues);

        for(Column column : nestedLists) {
            try {
                Field field = entity.getClass().getDeclaredField(column.getLabel());
                field.setAccessible(true);
                Object actualObject = field.get(entity);
                HashMap<String, Long> foreignKey = new HashMap<>();
                foreignKey.put("id_" + entity.getClass().getSimpleName(), rowId);
                insert(context, (List<U>)actualObject, foreignKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rowId;
    }

    /**
     * save a list of object in their corresponding tables
     * @param context the android context
     * @param list: list of object to save
     * @param <T> type of the entity to insert
     * @return the list of rows id
     */
    public <T> List<Long> insert(Context context, List<T> list, HashMap<String, Long> foreignKeys) throws IOException {
        if (list.size() <= 0) return null;

        List<Long> rows = new ArrayList<>();
        for(T entity : list) {
            rows.add(insert(context, entity, foreignKeys));
        }
        return rows;
    }

    /**
     * fetch all the row of the table
     * @param context the android context
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param condition: WHERE condition, example: "id=3". Can be null
     * @param <T> type of the entity to fetch
     * @return a list containing all the object saved in the table
     */
    public <T> List<T> fetchAll(Context context, Class<T> classType, String condition) throws IOException {
        Cursor cursor = db.rawQuery(getFetchAllRequest(classType.getSimpleName(), condition), null);
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            T entity = getEntity(context, classType, cursor);
            entities.add(entity);
            next = cursor.moveToNext();
        } while (next);
        cursor.close();
        return entities;
    }

    /**
     * fetch all the row of the table and return the first
     * @param context the android context
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param condition: WHERE condition, example: "id=3". Can be null
     * @param <T> type of the entity to fetch
     * @return the first object saved in the table
     */
    public <T> T fetchFirst(Context context, Class<T> classType, String condition) throws IOException {
        Cursor cursor = db.rawQuery(getFetchAllRequest(classType.getSimpleName(), condition), null);
        if (cursor == null || cursor.getCount() <= 0) return null;
        cursor.moveToFirst();
        T entity = getEntity(context, classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * fetch the row of the table that has the corresponding ID than the one provided
     * @param context the android context
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param <T> type of the entity to fetch
     * @param id: the id to look for
     * @return the object saved in db with the corresponding id
     */
    public <T> T fetchById(Context context, Class<T> classType, String id) throws IOException {
        Cursor cursor = db.rawQuery(getFetchByIdRequest(classType.getSimpleName(), id), null);
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = getEntity(context, classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * fetch the row of the table that is at the corresponding row id than the one provided
     * @param context the android context
     * @param classType: the class corresponding to the table where rows should be fetched
     * @param id: the row id to look for
     * <p>The row id or ROWID is an hidden field generated by Sqlite for each row of every table. It correspond to
     * a long value.</p>
     * @param <T> type of the entity to fetch
     * @return the object saved in the db with the corresponding rowid
     */
    public <T> T fetchByRowId(Context context, Class<T> classType, long id) throws IOException {
        Cursor cursor = db.rawQuery(getFetchByRowIdRequest(classType.getSimpleName(), id), null);
        if (cursor == null || cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        T entity = getEntity(context, classType, cursor);
        cursor.close();
        return entity;
    }

    /**
     * update the row corresponding to the entity passed by argument
     * @param context the android context
     * @param entity the object to update
     * @param <T> type of the entity to fetch
     * @return the number of row affected
     */
    public <T> long update(Context context, T entity) throws IOException {
        String id = getEntityId(entity);
        if (!id.equals("-1")) {
            return update(context, entity, id);
        } else {
            return 0;
        }
    }

    /**
     * Save the entity in db if it does not exist or update it otherwise.
     * @param context the android context
     * @param entity the object to save or update
     * @param <T> type of the entity to fetch
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted
     */
    public <T> long saveOrUpdate(Context context, T entity) throws IOException {
        String id = getEntityId(entity);
        if (!id.equals("-1")) {
            Cursor cursor = db.rawQuery(getFetchByIdRequest(entity.getClass().getSimpleName(), id), null);
            if (cursor != null && cursor.getCount() > 0) {
                update(context, entity, id);
                return 0;
            } else {
                return insert(context, entity, null);
            }
        } else {
            return -1;
        }
    }

    /**
     * for each item in the list, it save it in db if it does not exist, or update it otherwise.
     * @param context the android context
     * @param list the list of object to save or update
     * @param <T> type of the entity to fetch
     * @return for each items, -1 if it failed, 0 if a row was updated or the rowid if it inserted
     */
    public <T> List<Long> saveOrUpdate(Context context, List<T> list) throws IOException {
        if (list.size() <= 0) return null;
        List<Long> results = new ArrayList<>();
        for(T entity : list) {
            results.add(saveOrUpdate(context, entity));
        }
        return results;
    }

    /**
     * fetch all the row of a table and return the number of items in the cursor
     * @param tableName the name of the table that is counted
     * @return the number of row in the cursor
     */
    public Integer getTableCount(String tableName) {
        Cursor cursor = db.rawQuery(getFetchAllRequest(tableName, null), null);
        return cursor.getCount();
    }

    private String getFetchAllRequest(String table, String condition) {
        return new QueryBuilder()
                .select("ROWID", "*")
                .from(table)
                .where(condition)
                .toSqlRequest();
    }

    private String getFetchByIdRequest(String table, String id) {
        return new QueryBuilder()
                .select("ROWID", "*")
                .from(table)
                .where("id=" + id)
                .toSqlRequest();
    }

    private String getFetchByRowIdRequest(String table, long rowId) {
        return new QueryBuilder()
                .select("ROWID", "*")
                .from(table)
                .where("ROWID=" + rowId)
                .toSqlRequest();
    }

    private <T, U> T getEntity(Context context, Class<T> classType, Cursor cursor) throws IOException {
        T entity = EntityBuilder.bindCursorToEntity(context, classType, cursor);

        Table table = SchemaHelper.getInstance().getTable(context, classType.getSimpleName());
        if (table == null) return null;

        for (Column column : table.getColumns()) {
            if (column.isList() && column.isCustom()) {
                try {
                    int rowIdIdx = cursor.getColumnIndex("rowid");
                    if (rowIdIdx == -1) continue;
                    long id = cursor.getLong(rowIdIdx);
                    Field field = getFieldFromEntity(entity, column.getLabel());
                    List<U> list = (List<U>)fetchAll(context, ObjectHelper.getActualListType(field),
                            "id_" + classType.getSimpleName() + "=" + String.valueOf(id));
                    field.set(entity, list);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (!column.isList() && column.isCustom()) {
                try {
                    int idx = cursor.getColumnIndex(column.getLabel());
                    long rowId = cursor.getLong(idx);
                    Field field = getFieldFromEntity(entity, column.getLabel());
                    if (field != null) {
                        U object = (U)fetchFirst(context, field.getType(), "ROWID="+rowId);
                        field.set(entity, object);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return entity;
    }

    private <T> Field getFieldFromEntity(T entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> long update(Context context, T entity, String id) throws IOException {
        return db.update(entity.getClass().getSimpleName(), ObjectHelper.getEntityContentValues(context, entity), id);
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
