package no.hyper.memoryorm;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 01.06.2016.
 */
public class ObjectHelper {

    private static String THIS = "this";

    /**
     * return the list of fields declared in the class without the `this` implicit field
     */
    public static List<Field> getDeclaredFields(Class classType) {
        Field[] all = classType.getDeclaredFields();
        List<Field> fields = new ArrayList<>();
        for(int i = 0; i < all.length; i++) {
            if (all[i].getName().contains(THIS)) continue;
            fields.add(all[i]);
        }
        return fields;
    }

    /**
     * Return true or false if the field pass as parameter is a custom type
     */
    public static <T> boolean isCustomType(String classname) {
        try {
            Class c = Class.forName("java.lang." + classname);
            if (c.isPrimitive() || c != null) return false;
            else return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * return true if the field type is List, else false
     */
    public static boolean isAList(Field field) {
        return field.getType().getSimpleName().equals(List.class.getSimpleName());
    }

    /**
     * return a list of field for which the type is List
     * @param tableName: the class containing the attibutes to test
     */
    public static List<Column> getCustomListColumns(String tableName) {
        Table table = SchemaHelper.getInstance().getTable(tableName);
        List<Column> columns = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (column.isList() && isCustomType(getEquivalentJavaType(column.getType()))) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * return a list of field that have a custom type
     * @param tableName: the class containing the attibutes to test
     */
    public static List<Column> getNestedObjects(String tableName) {
        Table table = SchemaHelper.getInstance().getTable(tableName);
        List<Column> columns = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (!column.isList() && isCustomType(getEquivalentJavaType(column.getType()))) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * return the equivalent sql type of a java type
     */
    public static <T> String getEquivalentSqlType(Class<T> classType) {
        if (isCustomType(classType.getSimpleName())) return "INTEGER";
        switch (classType.getSimpleName()) {
            case "boolean" :
            case "int": return "INTEGER";
            default : return "TEXT";
        }
    }

    /**
     * return the equivalent sql type of a java type
     */
    public static String getEquivalentJavaType(String sqlType) {
        switch (sqlType) {
            case "integer" : return "Integer";
            case "text" : return "String";
            default : return  sqlType;
        }
    }

    /**
     * Return the the sql request part describing the columns of a table. To use with sql request acting on database
     * tables like CREATE.
     * <p>If one field is named "id", the key word "PRIMARY KEY" will be automatically added to the sql description of
     * the column</p>
     * @param fields: The fields used to create the columns
     * @param foreignKeys: If not null, this value will the foreign keys value in the sql description
     * @return A sql string describing every columns needed in a table
     */
    public static String getEquivalentSqlContent(List<Field> fields, List<String> foreignKeys) {
        StringBuilder sb = new StringBuilder();
        for(Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("id")){
                String meta = getEquivalentSqlType(field.getType()) +" PRIMARY KEY,";
                sb.append(fieldName + " " + meta);
            } else {
                sb.append(fieldName + " " + getEquivalentSqlType(field.getType()) + ",");
            }
        }
        if (foreignKeys != null && foreignKeys.size() > 0) {
            for(String foreignKey : foreignKeys) {
                sb.append("rowId_" + foreignKey + " INTEGER,");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * return the class type of object contained by the list
     */
    public static <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    public static <T, U> ContentValues getEntityContentValues(T entity) {
        Class c = entity.getClass();
        Table table = SchemaHelper.getInstance().getTable(c.getSimpleName());
        ContentValues values = new ContentValues();
        for(Column column : table.getColumns()) {
            try {
                Field field = c.getDeclaredField(column.getLabel());
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value == null || isCustomType(getEquivalentJavaType(column.getType()))) {
                    continue;
                } else if(column.isList()) {
                    List<U> list = (List<U>)value;
                    StringBuilder builder = new StringBuilder();
                    for (U item : list) {
                        builder.append(item + "|");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    values.put(column.getLabel(), builder.toString());
                } else {
                    values.put(column.getLabel(), convertJavaValueToSQLite(value).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    public static Object convertJavaValueToSQLite(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Boolean" : return ((boolean)value) ? 1 : 0;
            default : return value;
        }
    }

}
