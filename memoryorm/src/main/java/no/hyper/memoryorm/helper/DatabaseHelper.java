package no.hyper.memoryorm.helper;

import android.content.Context;

import java.io.IOException;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Database;
import no.hyper.memoryorm.model.Table;

/**
 * Created by Jean on 5/15/2016.
 */
public class DatabaseHelper {

    private DbManager db;
    private Context context;

    public DatabaseHelper(DbManager db, Context context) {
        this.db = db;
        this.context = context;
    }

    /**
     * create every table contained in the Database object
     */
    public void createTables() throws IOException {
        Database database = SchemaHelper.getInstance().getDatabase(context);
        for (Table table : database.getTables()) {
            createTable(table);
        }
    }

    /**
     * create the database represented by the object passed as parameter
     */
    private void createTable(Table table) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("(");
        for (Column column : table.getColumns()) {
            if (column.isEnum() || (column.isList() && !column.isCustom())) {
                content.append(column.getLabel());
                content.append(" text,");
            } else if (!column.isList() && column.isCustom()) {
                content.append(column.getLabel());
                createTable(SchemaHelper.getInstance().getTable(context, column.getType()));
                content.append(" integer,");
            } else if (!column.isList() && !column.isCustom()) {
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
     * delete every row of every tables represented by the Database Object
     */
    public void cleanTables() throws IOException {
        for (Table table : SchemaHelper.getInstance().getDatabase(context).getTables()) {
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
