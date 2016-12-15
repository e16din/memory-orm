package no.hyper.memoryorm.operation;

import android.content.ContentValues;
import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.helper.ObjectHelper;
import no.hyper.memoryorm.model.Column;

/**
 * Created by jean on 14.11.2016.
 */

public class InsertOperation {

    /**
     * save an object in the corresponding table. If the object has nested object/list of object, they will be save in
     * their corresponding table.
     * @param context the android context
     * @param entity: the object to save
     * @param foreignKeys: an hash map of foreign keys. The key represent the name of the column, the value is the id
     * @return the id of the row for the object inserted
     */
    public static <T> long insert(DbManager db, Context context, T entity, HashMap<String, Long> foreignKeys)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        List<Column> nestedLists = ObjectHelper.getCustomListColumns(context, entity.getClass().getSimpleName());
        List<Column> nestedObjects = ObjectHelper.getNestedObjects(context, entity.getClass().getSimpleName());
        ContentValues entityValues = ObjectHelper.getEntityContentValues(context, entity);

        if (foreignKeys != null) {
            for(Map.Entry<String, Long> key : foreignKeys.entrySet()) {
                entityValues.put(key.getKey(), key.getValue());
            }
        }

        for(Column column : nestedObjects) {
            Long id = insertNestedObject(db, entity, column, context);
            entityValues.put(column.getLabel(), id);
        }

        Long rowId = db.insert(entity.getClass().getSimpleName(), entityValues);

        for(Column column : nestedLists) {
            insertNestedList(db, entity, column.getLabel(), context, rowId);
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
    public static <T> List<Long> insert(DbManager db, Context context, List<T> list, HashMap<String, Long> foreignKeys)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        if (list != null && list.size() <= 0) return null;

        List<Long> rows = new ArrayList<>();
        for(T entity : list) {
            rows.add(insert(db, context, entity, foreignKeys));
        }
        return rows;
    }

    private static <T> Long insertNestedObject(DbManager db, T entity, Column column, Context context)
            throws NoSuchFieldException, IllegalAccessException, IOException {
        Field field = entity.getClass().getDeclaredField(column.getLabel());
        field.setAccessible(true);
        Object actualObject = field.get(entity);
        return insert(db, context, actualObject, null);
    }

    private static <T, U> void insertNestedList(DbManager db, T entity, String columnLabel, Context context, Long rowId)
            throws NoSuchFieldException, IllegalAccessException, IOException {
        Field field = entity.getClass().getDeclaredField(columnLabel);
        field.setAccessible(true);
        Object actualObject = field.get(entity);
        if (actualObject == null) return;

        HashMap<String, Long> foreignKey = new HashMap<>();
        foreignKey.put("id_" + entity.getClass().getSimpleName(), rowId);
        insert(db, context, (List<U>)actualObject, foreignKey);
    }

}
