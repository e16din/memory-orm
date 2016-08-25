package no.hyper.memoryorm.Helper;

import android.database.Cursor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;

/**
 * Created by Jean on 5/15/2016.
 */
public class EntityBuilder {

    /**
     * return an array of default values to use as parameters for a given constructor.
     * The constructor used is the first one obtained by reflection.
     * @param classes: Array of parameters' class.
     * @return: int = 0, boolean = false, String = "".
     */
    public static Object[] getDefaultConstructorParameters(Class<?>[] classes) {
        Object[] parameters = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            switch (classes[i].getSimpleName()) {
                case "int": parameters[i] = 0; break;
                case "boolean": parameters[i] = false; break;
                case "String": parameters[i] = ""; break;
                case "List": parameters[i] = new ArrayList<>();
            }
        }
        return parameters;
    }

    /**
     * bind the value of the hash map in the entity.
     * The key of the hash maps have to be equals to the name of the entity's attributes.
     * @param entity: the instance to bind the values into.
     * @param values: the non-custom values of the entity to bind
     * @param <T>
     * @return the entity with the values pass in the hash maps
     */
    public static <T> T bindHashMapToEntity(T entity, HashMap<String, Object> values) {
        for(Field field : ObjectHelper.getDeclaredFields(entity.getClass())) {
            try {
                Object value = values.get(field.getName());
                field.setAccessible(true);
                if (value == null) continue;
                String typeName = field.getType().getSimpleName();
                if (typeName.equals("boolean") || typeName.equals("Boolean")) {
                    field.set(entity, value.equals(1));
                } else {
                    field.set(entity, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return entity;
    }

    /**
     * return an object of type T binded with the values of the cursor passed.
     * @param classType: The type of the object that will be return.
     * @param cursor: cursor containing the values for the entity.
     * @param <T>
     */
    public static <T> HashMap<String, Object> bindCursorToHashMap(String jsonDb, Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = new HashMap<>();
        Table table = SchemaHelper.getInstance().getTable(jsonDb, classType.getSimpleName());

        for (Column column : table.getColumns()) {
            int index = cursor.getColumnIndex(column.getLabel());
            if (index >= 0) {
                if (column.isList()) {
                    String stringList = cursor.getString(index);
                    if (stringList != null && stringList != "") {
                        String[] array = stringList.split(";");
                        if (column.getType().equals("integer")) {
                            List<Integer> list = new ArrayList<>();
                            for(String item : array) {
                                list.add(Integer.valueOf(item));
                            }
                            map.put(column.getLabel(), list);
                        } else if (column.getType().equals("text")) {
                            List<String> list = new ArrayList<>();
                            for(String item : array) {
                                list.add(item);
                            }
                            map.put(column.getLabel(), list);
                        }
                    }
                } else if (column.getType().equals("text")) {
                    map.put(column.getLabel(), cursor.getString(index));
                } else if (column.getType().equals("integer")) {
                    map.put(column.getLabel(), cursor.getInt(index));
                }
            }
        }

        return map;
    }

    /**
     * return an object of type T binded with the values present in the cursor
     * @param classType: The type of the object that will be return.
     * @param cursor: cursor containing the values for the entity.
     * @param <T>
     */
    public static <T> T bindCursorToEntity(String jsonDb, Class<T> classType, Cursor cursor) {
        HashMap<String, Object> map = bindCursorToHashMap(jsonDb, classType, cursor);
        T entity = null;
        try {
            Constructor[] constructors = classType.getDeclaredConstructors();
            Constructor validConstructor = null;
            for (Constructor constructor : constructors) {
                boolean valid = true;
                for (Class c : constructor.getParameterTypes()) {
                    if (c.getSimpleName().equals("InstantReloadException") ||
                            c.getSimpleName().equals("DefaultConstructorMarker")||
                            c.getSimpleName().equals("Parcel")) {
                        valid = false;
                    }
                }
                if (valid) {
                    validConstructor = constructor;
                    break;
                }
            }
            if (validConstructor != null) {
                Object[] parameters = getDefaultConstructorParameters(validConstructor.getParameterTypes());
                entity = (T)validConstructor.newInstance(parameters);
                entity = bindHashMapToEntity(entity, map);
            }
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
