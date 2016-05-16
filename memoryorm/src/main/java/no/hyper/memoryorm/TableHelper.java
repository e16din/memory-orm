package no.hyper.memoryorm;

import android.database.Cursor;

import java.lang.reflect.Field;

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
            if (isCustomType(field)) {
                createTableIfNecessaryFrom(field.getType(), autoincrement);
            }
        }
        String request = getCreateTableRequest(classType, autoincrement);
        db.execute(request);
    }

    public <T> void deleteTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (isCustomType(field)) {
                deleteTable(field.getType());
            }
        }
        String request = getDeleteTableRequest(classType.getSimpleName());
        db.execute(request);
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

}
