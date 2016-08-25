package no.hyper.memoryorm.Helper;

import android.content.ContentValues;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.annotation.MemoryIgnore;
import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 01.06.2016.
 */
public class ObjectHelper {

    private static String THIS = "this";
    private static String CHANGE = "change";
    private static String COMPANION = "Companion";

    /**
     * return the list of fields declared in the class without the `this` implicit field
     */
    public static List<Field> getDeclaredFields(Class classType) {
        Field[] all = classType.getDeclaredFields();
        List<Field> fields = new ArrayList<>();
        outerloop : for(int i = 0; i < all.length; i++) {
            String name = all[i].getName();
            if (name.contains(THIS) || name.contains(CHANGE) || name.contains(COMPANION)) continue;
            Annotation[] annotations = all[i].getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(MemoryIgnore.class)) {
                    break outerloop;
                }
            }
            fields.add(all[i]);
        }

        return fields;
    }

    /**
     * Return true or false if the field pass as parameter is a custom type
     * @param classname the name of the class to test
     * @param <T>
     * @return true if the class is a custom one, false otherwise
     */
    public static <T> boolean isCustomType(String classname) {
        try {
            if (classname.equals("boolean") || classname.equals("int")) return false;
            Class c = Class.forName("java.lang." + classname);
            if (c.isPrimitive() || c != null) return false;
            else return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * return true if the field type is List, else false
     * @param field the field to test
     * @return true if the type of the field is a list, false otherwise
     */
    public static boolean isAList(Field field) {
        return field.getType().getSimpleName().equals(List.class.getSimpleName());
    }

    /**
     * return a list of field for which the type is List
     * @param tableName: the class containing the attibutes to test
     * @return a list containing the column that represent a list of custom type
     */
    public static List<Column> getCustomListColumns(String jsonDb, String tableName) {
        Table table = SchemaHelper.getInstance().getTable(jsonDb, tableName);
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
     * @return a list containing the column that represent a object of custom type
     */
    public static List<Column> getNestedObjects(String jsonDb, String tableName) {
        Table table = SchemaHelper.getInstance().getTable(jsonDb, tableName);
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
     * @param classType the type to look for an equivalent sql type
     * @param <T>
     * @return a string representing the equivalent sql type
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
     * @param sqlType the sql type to look for an java equivalent
     * @return a string representing an java equivalent
     */
    public static String getEquivalentJavaType(String sqlType) {
        switch (sqlType) {
            case "integer" : return "Integer";
            case "text" : return "String";
            default : return  sqlType;
        }
    }

    /**
     * return the class type of object contained by the list
     * @param list the list used to find the actual type
     * @param <T>
     * @return a class corresponding to the actual type
     */
    public static <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    /**
     * this function retrieve the class type object corresponding to the string parameter
     * @param className the full name of the class, package name included
     * @return a class type
     */
    public static <T> Class<T> getClassFromName(String className) {
        try {
            return (Class<T>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static <T, U> ContentValues getEntityContentValues(String jsonDb, T entity) {
        Class c = entity.getClass();
        Table table = SchemaHelper.getInstance().getTable(jsonDb, c.getSimpleName());
        ContentValues values = new ContentValues();
        for(Column column : table.getColumns()) {
            if (column.isForeignKey()) continue;

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
                        builder.append(item + ";");
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
