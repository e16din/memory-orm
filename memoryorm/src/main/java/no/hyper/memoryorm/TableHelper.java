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

    public <T> void createTableFrom(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().contains("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                createManyToOneRelationTable(classType, field);
            } else if (isCustomType(field)) {
                createTableFrom(field.getType());
            }
        }
        String request = getCreateTableRequest(classType, null);
        db.execute(request);
    }

    public <T> void deleteTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                deleteRelationTables(field);
            } else if (isCustomType(field)) {
                deleteTable(field.getType());
            }
        }
        db.execute(getDeleteTableRequest(classType.getSimpleName()));
    }

    private <T> void deleteRelationTables(Field list) {
        Class<T> actualListType = getActualListType(list);
        deleteTable(actualListType);
    }

    public <T> void emptyTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                emptyRelationTables(field);
            } else if (isCustomType(field)) {
                emptyTable(field.getType());
            }
        }
        db.execute("DELETE FROM " + classType.getSimpleName());
    }

    private <T> void emptyRelationTables(Field list) {
        Class<T> actualListType = getActualListType(list);
        emptyTable(actualListType);
    }

    private <T, U> void createManyToOneRelationTable(Class<T> classType, Field field) {
        Class<U> actualListType = getActualListType(field);
        String request = getCreateTableRequest(actualListType, classType.getSimpleName());
        db.execute(request);
    }

    private <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    private <T> String getCreateTableRequest(Class<T> classType, String foreignKey) {
        String content = getSqlTableContent(classType.getDeclaredFields(), foreignKey);
        return "CREATE TABLE IF NOT EXISTS " + classType.getSimpleName() + "(" + content + ");";
    }

    private String getSqlTableContent(Field[] fields, String foreignKey) {
        StringBuilder sb = new StringBuilder();
        for(Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("id")){
                String meta = getSQLPropertyType(field) +" PRIMARY KEY,";
                sb.append(fieldName + " " + meta);
            } else if (!fieldName.startsWith("$")) {
                sb.append(fieldName + " " + getSQLPropertyType(field) + ",");
            }
        }
        if (foreignKey != null) {
            sb.append("rowId_" + foreignKey + " INTEGER");
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
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
