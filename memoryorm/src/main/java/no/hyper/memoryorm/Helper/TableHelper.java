package no.hyper.memoryorm.Helper;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Table;

/**
 * Created by Jean on 5/15/2016.
 */
public class TableHelper {

    private DbManager db;
    private String jsonDb;

    public TableHelper(DbManager db, String jsonDb) {
        this.db = db;
        this.jsonDb = jsonDb;
    }

    /**
     * create every table contained in the Database object
     */
    public void createTables() {
        for (Table table : SchemaHelper.getInstance().getDatabase(jsonDb).getTables()) {
            createTable(table);
        }
    }

    /**
     * create the database represented by the object passed as parameter
     */
    public void createTable(Table table) {
        StringBuilder content = new StringBuilder();
        content.append("(");
        for (Column column : table.getColumns()) {
            if (!column.isList() && ObjectHelper.isCustomType(ObjectHelper.getEquivalentJavaType(column.getType()))) {
                content.append(column.getLabel());
                createTable(SchemaHelper.getInstance().getTable(jsonDb, column.getType()));
                content.append(" integer,");
            } else if (column.isList() && !ObjectHelper.isCustomType(ObjectHelper.getEquivalentJavaType(column.getType()))) {
                content.append(column.getLabel());
                content.append(" text,");
            } else if (!column.isList() && !ObjectHelper.isCustomType(ObjectHelper.getEquivalentJavaType(column.getType()))) {
                content.append(column.getLabel());
                content.append(" ");
                content.append(column.getType());
                if (column.isPrimary()) {
                    content.append(" primary key");
                }
                content.append(",");
            }
        }
        content.deleteCharAt(content.length() - 1);
        content.append(")");
        String request = "CREATE TABLE IF NOT EXISTS " + table.getName() + content.toString();
        db.execute(request);
    }

    /**
     * delete all the table represented by the Database object
     */
    public void deleteTables() {
        for (Table table : SchemaHelper.getInstance().getDatabase(jsonDb).getTables()) {
            deleteTable(table);
        }
    }

    /**
     * delete the table represented by the object passed as parameter
     */
    public void deleteTable(Table table) {
        String request = "DROP TABLE IF EXISTS " + table.getName();
        db.execute(request);
    }

    /**
     * delete every row of every tables represented by the Database Object
     */
    public void cleanTables() {
        for (Table table : SchemaHelper.getInstance().getDatabase(jsonDb).getTables()) {
            cleanTable(table.getName(), null);
        }
    }

    /**
     * delete one, several or all the rows in a table
     * @param tableName the name of the table where the delete must be done
     * @param clause a where clause to limit the effect of the command
     * @return a int representing the number of rows affected by the operation
     */
    public int cleanTable(String tableName, String clause) {
        return db.delete(tableName, clause);
    }

}
