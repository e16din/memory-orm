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

    /**
     * create a table in the database based on the class passed as parameter
     * @param classType: class to use to create the table
     */
    public <T> void createTableFrom(Class<T> classType) {
        for(Field field : ObjectHelper.getDeclaredFields(classType)) {
            if (ObjectHelper.isAList(field)) {
                createManyToOneRelationTable(classType, field);
            } else if (ObjectHelper.isCustomType(field.getType())) {
                createTableFrom(field.getType());
            }
        }
        String request = getSqlTableCreationRequest(classType, null);
        db.execute(request);
    }

    /**
     * create the a sql table based on a list variable.
     * <p>When an object has a list attribute, use this method to create a separate table to save the items inside the
     * list</p>
     * @param classType: the class that has the list attribute
     * @param field: the field from `classType` that is the list
     */
    public <T, U> void createManyToOneRelationTable(Class<T> classType, Field field) {
        Class<U> actualListType = ObjectHelper.getActualListType(field);
        String request = "";
        if (ObjectHelper.isCustomType(actualListType)) {
            request = getSqlTableCreationRequest(actualListType, classType.getSimpleName());
        } else {
            switch (actualListType.getSimpleName()) {
                case "String" : request = "CREATE TABLE IF NOT EXISTS String(rowId_" +
                        classType.getSimpleName() + " INTEGER, value TEXT);"; break;
                case "Integer" : request = "CREATE TABLE IF NOT EXISTS Integer(rowId_" +
                        classType.getSimpleName() + " INTEGER, value INTEGER);"; break;
            }
        }

        db.execute(request);
    }

    /**
     * delete the table based on the class passed as parameter, and its relation tables created from the class' list
     * and custom attributes
     * @param classType: class representing the table to delete
     */
    public <T> void deleteTable(Class<T> classType) {
        for(Field field : ObjectHelper.getDeclaredFields(classType)) {
            if (ObjectHelper.isAList(field)) {
                deleteRelationTables(field);
            } else if (ObjectHelper.isCustomType(field.getType())) {
                deleteTable(field.getType());
            }
        }
        db.execute("DROP TABLE IF EXISTS " + classType.getSimpleName() + ";");
    }

    /**
     * check if the field is a list, if so delete the correct table
     */
    public <T> void deleteRelationTables(Field field) {
        Class<T> actualListType = ObjectHelper.getActualListType(field);
        if (ObjectHelper.isCustomType(actualListType)) {
            deleteTable(field.getType());
        } else {
            switch (actualListType.getSimpleName()) {
                case "String" : db.execute("DROP TABLE IF EXISTS String;"); break;
                case "Integer" : db.execute("DROP TABLE IF EXISTS Integer;"); break;
            }
        }
    }

    /**
     * remove every row in a table
     * @param classType: class representing the table to empty
     */
    public <T> void emptyTable(Class<T> classType) {
        for(Field field : ObjectHelper.getDeclaredFields(classType)) {
            if (ObjectHelper.isAList(field)) {
                emptyRelationTables(field);
            } else if (ObjectHelper.isCustomType(field.getType())) {
                emptyTable(field.getType());
            }
        }
        db.execute("DELETE FROM " + classType.getSimpleName());
    }

    /**
     * delete table based on the actual list type
     * @param field the list field representing the table to empty
     */
    public <T> void emptyRelationTables(Field field) {
        Class<T> actualListType = ObjectHelper.getActualListType(field);
        if (ObjectHelper.isCustomType(actualListType)) {
            deleteTable(field.getType());
        } else {
            switch (actualListType.getSimpleName()) {
                case "String" : db.execute("DELETE FROM String"); break;
                case "Integer" : db.execute("DELETE FROM Integer");
            }
        }
    }

    /**
     * return the sql request to create a table
     * @param classType: class to use to create the table
     * @param foreignKey: foreign key to add to the table (nullable)
     * @return an sql request to execute
     */
    public <T> String getSqlTableCreationRequest(Class<T> classType, String foreignKey) {
        List<Field> fields = ObjectHelper.getDeclaredFields(classType);
        String content = ObjectHelper.getEquivalentSqlContent(fields, foreignKey);
        return "CREATE TABLE IF NOT EXISTS " + classType.getSimpleName() + "(" + content + ");";
    }

}
