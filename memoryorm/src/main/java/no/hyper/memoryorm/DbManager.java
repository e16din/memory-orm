package no.hyper.memoryorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jean on 5/12/2016.
 */
public class DbManager extends SQLiteOpenHelper {

    private final static String LOG_TAG = DbManager.class.getSimpleName();
    private Gson gson = new Gson();

    public DbManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "$LOG_TAG onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }




    private String createTable(String name, String content) {
        return "CREATE TABLE IF NOT EXISTS " + name + "(" + content + ");";
    }

    private String createContent(Field[] fields) {
        StringBuilder sb = new StringBuilder();
        for(Field field : fields) {
            String fieldName = getFieldName(field);
            if (fieldName.equals("id")){
                sb.append(fieldName + " " + getSQLPropertyType(field) + " PRIMARY KEY,");
            } else if (!fieldName.startsWith("$")) {
                sb.append(fieldName + " " + getSQLPropertyType(field) + ",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String getSQLPropertyType(Field property) {
        switch (property.getGenericType().toString()) {
            case "boolean" :
            case "int": return "INTEGER";
            default: return "TEXT";
        }
    }

    private Long insert(String table, ContentValues values) {
        return this.getWritableDatabase().insert(table, null, values);
    }

    private Cursor rawQuery(String request, String[] args) {
        return this.getWritableDatabase().rawQuery(request, args);
    }

    private String getFieldName(Field field) {
        SerializedName annotation = field.getAnnotation(SerializedName.class);
        if (annotation != null) {
            String name = annotation.value();
            return name.replace("-", "_");
        } else {
            return field.getName();
        }
    }

    private Cursor getEntityById(String table, String id) {
        String request = "SELECT * FROM " + table + " WHERE ID='" + id + "';";
        return rawQuery(request, null);
    }

    private <T> void createTableIfNecessary(Class<T> entity) {
        if (testIfTableExist(entity.getSimpleName())) return;

        String content = createContent(entity.getDeclaredFields());
        String table = createTable(entity.getSimpleName(), content);
        execute(table);
    }

    private <T> long insertWithNestedObject(T entity, HashMap<String, Type> nestedType) {
        try {
            ContentValues values = new ContentValues();
            for(Field field : entity.getClass().getDeclaredFields()) {
                String fieldName = getFieldName(field);
                if(!fieldName.startsWith("$")) {
                    field.setAccessible(true);
                    Object value = null;
                    try {
                        value = field.get(entity);
                        if (value != null) {
                            if (value.toString().equals("true")) {
                                value = 1;
                            } else if (value.toString().equals("false")) {
                                value = 0;
                            } else if (nestedType.keySet().contains(fieldName)) {
                                value = gson.toJson(value);
                            }
                        } else {
                            value = "";
                        }
                        values.put(fieldName, value.toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return insert(entity.getClass().getSimpleName(), values);
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    private <T> String cursorToString (Class<T> entityType, Cursor cursor) {
        cursor.moveToFirst();
        HashMap<String, Object> map = cursorToHashMap(entityType, cursor);
        map.remove("id");
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey() + "='" + entry.getValue() + "',");
        }
        sb.deleteCharAt(sb.length() - 1);
        return  sb.toString();
    }

    private <T> void updateOrInsert(T entity) {
        for(Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName() == "id") {
                try {
                    field.setAccessible(true);
                    String id = (String) field.get(entity);
                    Cursor cursor = getEntityById(entity.getClass().getSimpleName(), (String) field.get(entity));
                    if (cursor.getCount() > 0) {
                        String request = "UPDATE " + entity.getClass().getSimpleName() + " SET " + cursorToString(entity.getClass(), cursor) + " WHERE id='" + id + "';";
                        execute(request);
                    } else {
                        insert(entity);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private <T> void updateOrInsertWithNestedObject(T entity, HashMap<String, Type> nestedType) {
        for(Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName() == "id") {
                try {
                    field.setAccessible(true);
                    String id = (String) field.get(entity);
                    Cursor cursor = getEntityById(entity.getClass().getSimpleName(), (String) field.get(entity));
                    if (cursor.getCount() > 0) {
                        String request = "UPDATE " + entity.getClass().getSimpleName() + " SET " + cursorToString(entity.getClass(), cursor) + " WHERE id='" + id + "';";
                        execute(request);
                    } else {
                        insertWithNestedObject(entity, nestedType);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }





    public <T> List<T> fetchAllWithNestedObject(Class<T> entityType, List<String> nestedAttributes) {
        try {
            String query = "SELECT * FROM " + entityType.getSimpleName() + ";";
            Cursor cursor = rawQuery(query, null);
            if (cursor.getCount() <= 0) return null;

            cursor.moveToFirst();
            boolean next;
            List<T> entities = new ArrayList<>();
            JsonParser parser = new JsonParser();

            do {
                HashMap<String, Object> map = cursorToHashMap(entityType, cursor);
                for(Map.Entry<String, Object> entry : map.entrySet()) {
                    if(nestedAttributes.contains(entry.getKey())) {
                        map.put(entry.getKey(), (parser.parse((String)entry.getValue())));
                    }
                }
                String json = gson.toJson(map).replace("_", "-");
                entities.add(gson.fromJson(json, entityType));
                next = cursor.moveToNext();
            } while (next);
            return entities;
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    public <T> T fetchById(Class<T> entityType, String id) {
        try {
            Cursor cursor = getEntityById(entityType.getSimpleName(), id);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                HashMap<String, Object> map = cursorToHashMap(entityType, cursor);
                String json = gson.toJson(map).replace("_", "-");
                return gson.fromJson(json, entityType);
            } else {
                return null;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    public <T> void saveList(List<T> list) {
        try {
            createTableIfNecessary(list.get(0).getClass());
            ContentValues values = new ContentValues();
            for(T entity : list) {
                insert(entity);
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public <T> void updateOrInsertList(List<T> list) {
        try {
            if (list.size() > 0) {
                createTableIfNecessary(list.get(0).getClass());
                for(T entity : list) {
                    updateOrInsert(entity);
                }
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public <T> void updateOrInsertListWithNestedObject(List<T> list, HashMap<String, Type> nestedType) {
        try {
            createTableIfNecessary(list.get(0).getClass());
            for(T entity : list) {
                try {
                    updateOrInsertWithNestedObject(entity, nestedType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public <T> void updateOrInsertEntity(T entity) {
        try {
            createTableIfNecessary(entity.getClass());
            updateOrInsert(entity);
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------------------
    // REFACTORING ->

    private int execute(String request) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(request);
            db.close();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private <T> long insert(T entity) {
        ContentValues values = new ContentValues();
        for(Field field : entity.getClass().getDeclaredFields()) {
            if(field.getName().startsWith("$")) continue;
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(entity);
                if (value == null) continue;

                values.put(field.getName(), convertJavaValueToSQLite(value).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return insert(entity.getClass().getSimpleName(), values);
    }

    private Object convertJavaValueToSQLite(Object value) {
        if (value.toString().equals("true")) {
            return 1;
        } else if (value.toString().equals("false")) {
            return 0;
        } else {
            return value;
        }
    }

    private boolean testIfTableExist(String tableName) {
        Cursor cursor = rawQuery(SQLiteRequestHelper.testTableExistence(tableName), null);
        return (cursor.getCount() > 0);
    }

    private Object[] getDefaultParametersForConstructor(Class<?>[] types) {
        Object[] parameters = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i].getSimpleName()) {
                case "int": parameters[i] = 0; break;
                case "boolean": parameters[i] = false; break;
                case "String": parameters[i] = ""; break;
            }
        }
        return parameters;
    }

    private <T> T bindHashMapToEntity(HashMap<String, Object> map, T entity) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            for(Field field : entity.getClass().getDeclaredFields()) {
                if (field.getName().equals(entry.getKey())) {
                    try {
                        field.setAccessible(true);
                        field.set(entity, entry.getValue());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return entity;
    }

    private <T> HashMap<String, Object> cursorToHashMap(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : classType.getDeclaredFields()) {
            String fieldName = field.getName();
            int index = cursor.getColumnIndex(fieldName);
            if (index >= 0) {
                switch (field.getType().getSimpleName()) {
                    case "int":
                    case "Integer": map.put(fieldName, cursor.getInt(index)); break;
                    case "boolean": map.put(fieldName, cursor.getInt(index) == 1); break;
                    default: map.put(fieldName, cursor.getString(index)); break;
                }
            }
        }
        return map;
    }

    public <T> int createTableFrom(Class<T> classType, boolean autoincrement) {
        if (testIfTableExist(classType.getSimpleName())) return Memory.TABLE_ALREADY_EXIST;
        return execute(SQLiteRequestHelper.createTable(classType, autoincrement));
    }

    public <T> int deleteTable(Class<T> classType) {
        return execute(SQLiteRequestHelper.deleteTable(classType.getSimpleName()));
    }

    public <T> long save(T entity) {
        createTableIfNecessary(entity.getClass());
        return insert(entity);
    }

    public <T> List<T> fetchAll(Class<T> classType) {
        Cursor cursor = rawQuery(SQLiteRequestHelper.fetchAll(classType.getSimpleName()), null);
        if (cursor.getCount() <= 0) return null;

        cursor.moveToFirst();
        boolean next;
        List<T> entities = new ArrayList<>();

        do {
            HashMap<String, Object> map = cursorToHashMap(classType, cursor);
            //String json = gson.toJson(map).replace("_", "-");
            //entities.add(gson.fromJson(json, classType));
            try {
                Constructor constructor = classType.getDeclaredConstructors()[0];
                Object[] parameters = getDefaultParametersForConstructor(constructor.getParameterTypes());
                T entity = (T)classType.getDeclaredConstructors()[0].newInstance(parameters);
                entities.add(bindHashMapToEntity(map, entity));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            next = cursor.moveToNext();
        } while (next);
        return entities;
    }
}

