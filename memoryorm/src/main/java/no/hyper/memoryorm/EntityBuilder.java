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
public class EntityBuilder {

    /**
     * return an array of default values to use as parameters for a given constructor.
     * <p>The constructor used is the first one obtained by reflection.</p>
     * @param classes: Array of parameters' class.
     * @return: int -> 0, boolean -> false, String -> "".
     */
    public static Object[] getDefaultConstructorParameters(Class<?>[] classes) {
        Object[] parameters = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            switch (classes[i].getSimpleName()) {
                case "int": parameters[i] = 0; break;
                case "boolean": parameters[i] = false; break;
                case "String": parameters[i] = ""; break;
            }
        }
        return parameters;
    }

    /**
     * bind the value of the hash map in the entity.
     * <p>The key of the hash maps have to be equals to the name of the entity's attributes.</p>
     * @param entity: the instance to bind the values into.
     * @param values: the non-custom values of the entity to bind
     * @return the entity with the values pass in the hash maps
     */
    public static <T> T bindHashMapToEntity(T entity, HashMap<String, Object> values) {
        for(Field field : ObjectHelper.getDeclaredFields(entity.getClass())) {
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

    /**
     * return an object of type T binded with the values of the cursor passed.
     * @param classType: The class of the entity that will be return.
     * @param cursor: cursor containing the values for the entity.
     * @return: an entity binded with the cursor values.
     */
    public static <T> HashMap<String, Object> bindCursorToHashMap(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : ObjectHelper.getDeclaredFields(classType)) {
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

    public static <T> T bindCursorToEntity(Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = bindCursorToHashMap(classType, cursor);
        T entity = null;
        try {
            Constructor constructor = classType.getDeclaredConstructors()[0];
            Object[] parameters = getDefaultConstructorParameters(constructor.getParameterTypes());
            entity = (T)constructor.newInstance(parameters);
            entity = bindHashMapToEntity(entity, map);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return entity;
    }

}
