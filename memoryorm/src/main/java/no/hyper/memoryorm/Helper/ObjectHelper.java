package no.hyper.memoryorm.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.builder.EntityBuilder;
import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;
import no.hyper.memoryorm.operation.FetchOperation;

/**
 * Created by jean on 01.06.2016.
 */
public class ObjectHelper {

    /**
     * return the list of fields declared in the class without the `this` implicit field
     */
    public static List<String> getDeclaredFields(Context context, String tableName) throws IOException {
        Table table = SchemaHelper.getInstance().getTable(context, tableName);
        List<String> fields = new ArrayList<>();
        for (Column column : table.getColumns()) {
             fields.add(column.getLabel());
        }
        return fields;
    }

    /**
     * return a list of containing all the list field
     * @param tableName: the class containing the attibutes to test
     * @return a list containing the column that represent a list of custom type
     */
    public static List<Column> getCustomListColumns(Context context, String tableName) throws IOException {
        Table table = SchemaHelper.getInstance().getTable(context, tableName);
        List<Column> columns = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (column.isList() && column.isCustom()) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * return a list of field that have a custom type
     * @param tableName: the class containing the attibutes to test
     * @return a list containing the column that represent a object of custom type
     */
    public static List<Column> getNestedObjects(Context context, String tableName) throws IOException {
        Table table = SchemaHelper.getInstance().getTable(context, tableName);
        List<Column> columns = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (!column.isList() && column.isCustom()) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     *
     * @param entity
     * @param <T>
     * @param <U>
     * @return
     */
    public static <T, U> ContentValues getEntityContentValues(Context context, T entity) throws IOException {
        Class c = entity.getClass();
        Table table = SchemaHelper.getInstance().getTable(context, c.getSimpleName());
        ContentValues values = new ContentValues();
        for(Column column : table.getColumns()) {
            if (column.isForeignKey()) continue;

            try {
                Field field = c.getDeclaredField(column.getLabel());
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value == null || column.isCustom()) {
                    continue;
                } else if(column.isList()) {
                    List<U> list = (List<U>)value;
                    StringBuilder builder = new StringBuilder();
                    for (U item : list) {
                        builder.append(item + ";");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    values.put(column.getLabel(), builder.toString());
                } else if (column.isEnum()) {

                } else {
                    values.put(column.getLabel(), convertJavaValueToSQLite(value).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    /**
     * return the value of the id field of the object, if it has one
     * @param entity
     * @param <T>
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> String getEntityId(T entity) throws NoSuchFieldException, IllegalAccessException {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        return (String)field.get(entity);
    }

    /**
     *
     * @param db instance of DbManager
     * @param context context from the app
     * @param classType the type of the entity to get
     * @param cursor result from a db select request
     * @param <T> the type of the entity to get
     * @param <U> the type of nested custom list
     * @return the object instantiated and containing the values from the cursor
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static <T, U> T getEntity(DbManager db, Context context, Class<T> classType, Cursor cursor)
            throws IOException, IllegalAccessException, NoSuchFieldException, InvocationTargetException,
            InstantiationException {
        T entity = EntityBuilder.bindCursorToEntity(context, classType, cursor);

        Table table = SchemaHelper.getInstance().getTable(context, classType.getSimpleName());
        if (table == null) return null;

        for (Column column : table.getColumns()) {
            if (column.isList() && column.isCustom()) {
                int rowIdIdx = cursor.getColumnIndex("rowid");
                if (rowIdIdx == -1) continue;
                long id = cursor.getLong(rowIdIdx);
                Field field = getFieldFromEntity(entity, column.getLabel());
                String condition = "id_" + classType.getSimpleName() + "=" + String.valueOf(id);
                List<U> list = (List<U>) FetchOperation.fetchAll(db, context, getActualListType(field), condition);
                field.set(entity, list);
            } else if (!column.isList() && column.isCustom()) {
                int idx = cursor.getColumnIndex(column.getLabel());
                long rowId = cursor.getLong(idx);
                Field field = getFieldFromEntity(entity, column.getLabel());
                U object = (U)FetchOperation.fetchFirst(db, context, field.getType(), "ROWID="+rowId);
                field.set(entity, object);
            }
        }

        return entity;
    }

    private static <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    private static <T> Field getFieldFromEntity(T entity, String fieldName) throws NoSuchFieldException {
        Field field = entity.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private static Object convertJavaValueToSQLite(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Boolean" : return ((boolean)value) ? 1 : 0;
            default : return value;
        }
    }

}
