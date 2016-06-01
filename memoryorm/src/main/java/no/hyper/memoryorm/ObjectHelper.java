package no.hyper.memoryorm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    public static boolean isCustomType(Field field) {
        try {
            if (field.getType().isPrimitive()) return false;
            Class.forName("java.lang." + field.getType().getSimpleName());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * return the equivalent sql type of a java type
     * @return boolean, int and custom type return INTEGER. Everything else return TEXT
     */
    public static String getEquivalentSqlType(Field field) {
        if (isCustomType(field)) return "INTEGER";
        switch (field.getType().getSimpleName()) {
            case "boolean" :
            case "int": return "INTEGER";
            default : return "TEXT";
        }
    }

    /**
     * Return the the sql request part describing the columns of a table. To use with sql request acting on database
     * tables like CREATE.
     * <p>If one field is named "id", the key word "PRIMARY KEY" will be automatically added to the sql description of
     * the column</p>
     * @param fields: The fields used to create the columns
     * @param foreignKey: If not null, this value will add a foreign key value in the description
     * @return A sql string describing every columns needed in a table
     */
    public static String getEquivalentSqlContent(List<Field> fields, String foreignKey) {
        StringBuilder sb = new StringBuilder();
        for(Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("id")){
                String meta = getEquivalentSqlType(field) +" PRIMARY KEY,";
                sb.append(fieldName + " " + meta);
            } else {
                sb.append(fieldName + " " + getEquivalentSqlType(field) + ",");
            }
        }
        if (foreignKey != null) {
            sb.append("rowId_" + foreignKey + " INTEGER");
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

}
