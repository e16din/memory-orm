package no.hyper.memoryorm;

import android.database.Cursor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jean on 5/15/2016.
 */
public class CursorHelper {

    public static <T> T cursorToEntity(Class<T> classType, Cursor cursor, HashMap<String, Object> nestedObjects) {
        HashMap<String, Object> map = cursorToHashMap(classType, cursor);
        T entity = null;
        try {
            Constructor constructor = classType.getDeclaredConstructors()[0];
            Object[] parameters = getDefaultParametersForConstructor(constructor.getParameterTypes());
            entity = (T)classType.getDeclaredConstructors()[0].newInstance(parameters);
            entity = bindHashMapToEntity(map, entity, nestedObjects);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return entity;
    }

    private static <T> HashMap<String, Object> cursorToHashMap(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : classType.getDeclaredFields()) {
            String fieldName = field.getName();
            int index = cursor.getColumnIndex(fieldName);
            if (index >= 0) {
                switch (field.getType().getSimpleName()) {
                    case "int":
                    case "Integer": map.put(fieldName, cursor.getInt(index)); break;
                    case "boolean": map.put(fieldName, cursor.getInt(index) == 1); break;
                    case "String" : map.put(fieldName, cursor.getString(index)); break;
                    default: break;
                }
            }
        }
        return map;
    }

    private static Object[] getDefaultParametersForConstructor(Class<?>[] types) {
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

    private static <T> T bindHashMapToEntity(HashMap<String, Object> values, T entity,
                                             HashMap<String, Object> nestedObjects) {
        if (nestedObjects != null) {
            for(Map.Entry<String, Object> nestedObject : nestedObjects.entrySet()) {
                values.put(nestedObject.getKey(), nestedObject.getValue());
            }
        }

        for(Field field : entity.getClass().getDeclaredFields()) {
            try {
                Object value = values.get(field.getName());
                field.setAccessible(true);
                if (value == null) continue;
                field.set(entity, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

}
