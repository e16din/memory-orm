package no.hyper.memoryorm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by Jean on 5/15/2016.
 */
public class TableHelper {

    private DbManager db;

    public TableHelper(DbManager db) {
        this.db = db;
    }

    public <T> void createTableIfNecessaryFrom(Class<T> classType, boolean autoincrement) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                createLiaisonTables(classType, field, autoincrement);
            } else if (isCustomType(field)) {
                createTableIfNecessaryFrom(field.getType(), autoincrement);
            }
        }
        db.execute(getCreateTableRequest(classType, autoincrement));
    }

    public <T> void deleteTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                deleteLiaisonTables(classType, field);
            } else if (isCustomType(field)) {
                deleteTable(field.getType());
            }
            if (isCustomType(field)) {
                deleteTable(field.getType());
            }
        }
        db.execute(getDeleteTableRequest(classType.getSimpleName()));
    }

    private <T, U> void deleteLiaisonTables(Class<T> classType, Field list) {
        Class<U> actualListType = getActualListType(list);
        db.execute(getDeleteJunctionTableRequest(classType.getSimpleName(), actualListType.getSimpleName()));
        deleteTable(actualListType);
    }

    private <T, U> void createLiaisonTables(Class<T> classType, Field list, boolean autoincrement) {
        Class<U> actualListType = getActualListType(list);
        db.execute(getCreateJunctionTableRequest(classType.getSimpleName(), actualListType.getSimpleName()));
        createTableIfNecessaryFrom(actualListType, autoincrement);
    }

    private <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    private <T> String getCreateTableRequest(Class<T> classType, boolean autoIncrement) {
        StringBuilder sb = new StringBuilder();
        for(Field field : classType.getDeclaredFields()) {
            String fieldName = field.getName();
            if (fieldName.equals("id")){
                String meta = getSQLPropertyType(field) +"PRIMARY KEY,";
                if (autoIncrement) {
                    meta = "INTEGER PRIMARY KEY AUTOINCREMENT,";
                }
                sb.append(fieldName + " " + meta);
            } else if (!fieldName.startsWith("$")) {
                sb.append(fieldName + " " + getSQLPropertyType(field) + ",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return "CREATE TABLE IF NOT EXISTS " + classType.getSimpleName() + "(" + sb.toString() + ");";
    }

    private String getCreateJunctionTableRequest(String leftTable, String rightTable) {
        return "CREATE TABLE IF NOT EXISTS " + leftTable + "_" + rightTable + " (id_" + leftTable + " INTEGER, "
                + " id_" + rightTable +" INTEGER);";
    }

    private boolean isCustomType(Field field) {
        try {
            if (field.getType().isPrimitive()) {
                return false;
            }
            Class.forName("java.lang." + field.getType().getSimpleName());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private String getSQLPropertyType(Field field) {
        if (isCustomType(field)) return "INTEGER";
        switch (field.getType().getSimpleName()) {
            case "boolean" :
            case "int": return "INTEGER";
            default : return "TEXT";
        }
    }

    private static String getDeleteTableRequest(String name) {
        return "DROP TABLE IF EXISTS " + name + ";";
    }

    private String getDeleteJunctionTableRequest(String leftTable, String rightTable) {
        return "DROP TABLE IF EXISTS " + leftTable + "_" + rightTable + ";";
    }

}
