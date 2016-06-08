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
        for(Field field : ObjectHelper.getDeclaredFields(classType)) {
            if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                createManyToOneRelationTable(classType, field);
            } else if (ObjectHelper.isCustomType(field)) {
                createTableFrom(field.getType());
            }
        }
        String request = getSqlTableCreationRequest(classType, null);
        db.execute(request);
    }

    public <T> void deleteTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                deleteRelationTables(field);
            } else if (ObjectHelper.isCustomType(field)) {
                deleteTable(field.getType());
            }
        }
        db.execute(getSqlTableDeletionRequest(classType.getSimpleName()));
    }

    public <T> void deleteRelationTables(Field list) {
        Class<T> actualListType = getActualListType(list);
        deleteTable(actualListType);
    }

    public <T> void emptyTable(Class<T> classType) {
        for(Field field : classType.getDeclaredFields()) {
            if (field.getName().startsWith("$")) {
                continue;
            } else if (field.getType().getSimpleName().equals(List.class.getSimpleName())) {
                emptyRelationTables(field);
            } else if (ObjectHelper.isCustomType(field)) {
                emptyTable(field.getType());
            }
        }
        db.execute("DELETE FROM " + classType.getSimpleName());
    }

    public <T> void emptyRelationTables(Field list) {
        Class<T> actualListType = getActualListType(list);
        emptyTable(actualListType);
    }

    public <T, U> void createManyToOneRelationTable(Class<T> classType, Field field) {
        Class<U> actualListType = getActualListType(field);
        String request = getSqlTableCreationRequest(actualListType, classType.getSimpleName());
        db.execute(request);
    }

    public <T> Class<T> getActualListType(Field list) {
        ParameterizedType listType = (ParameterizedType) list.getGenericType();
        return (Class<T>) listType.getActualTypeArguments()[0];
    }

    public <T> String getSqlTableCreationRequest(Class<T> classType, String foreignKey) {
        List<Field> fields = ObjectHelper.getDeclaredFields(classType);
        String content = ObjectHelper.getEquivalentSqlContent(fields, foreignKey);
        return "CREATE TABLE IF NOT EXISTS " + classType.getSimpleName() + "(" + content + ");";
    }

    public static String getSqlTableDeletionRequest(String name) {
        return "DROP TABLE IF EXISTS " + name + ";";
    }

}
