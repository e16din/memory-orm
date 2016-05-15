package no.hyper.memoryorm;

import android.database.Cursor;

import java.lang.reflect.Field;

/**
 * Created by Jean on 5/15/2016.
 */
public class TableHelper {

    public static final int TABLE_ALREADY_EXIST = 10;
    private DbManager db;

    public TableHelper(DbManager db) {
        this.db = db;
    }

    public <T> int createTableFrom(Class<T> classType, boolean autoincrement) {
        if (testIfTableExist(classType.getSimpleName())) return TABLE_ALREADY_EXIST;
        return db.execute(createTable(classType, autoincrement));
    }

    public <T> int deleteTable(Class<T> classType) {
        return db.execute(deleteTable(classType.getSimpleName()));
    }

    private boolean testIfTableExist(String tableName) {
        Cursor cursor = db.rawQuery(testTableExistence(tableName), null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private String testTableExistence(String table) {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "';";
    }

    private <T> String createTable(Class<T> entityType, boolean autoIncrement) {
        StringBuilder sb = new StringBuilder();
        for(Field field : entityType.getDeclaredFields()) {
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
        return "CREATE TABLE IF NOT EXISTS " + entityType.getSimpleName() + "(" + sb.toString() + ");";
    }

    private String getSQLPropertyType(Field property) {
        switch (property.getGenericType().toString()) {
            case "boolean" :
            case "int": return "INTEGER";
            default: return "TEXT";
        }
    }

    public static String deleteTable(String name) {
        return "DROP TABLE IF EXISTS " + name + ";";
    }
}
