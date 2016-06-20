package no.hyper.memoryorm;

import com.google.gson.Gson;

import org.json.JSONException;
import java.lang.reflect.Field;
import java.util.List;

import no.hyper.memoryorm.model.Column;
import no.hyper.memoryorm.model.Database;
import no.hyper.memoryorm.model.Table;

/**
 * Created by Jean on 5/15/2016.
 */
public class TableHelper {

    private DbManager db;

    public TableHelper(DbManager db) {
        this.db = db;
    }

    /**
     * create every table contained in the Database object
     */
    public void createTables() {
        for (Table table : SchemaHelper.getInstance().getDatabase().getTables()) {
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
            content.append(column.getLabel());
            if (!column.isList() && ObjectHelper.isCustomType(ObjectHelper.getEquivalentJavaType(column.getType()))) {
                createTable(SchemaHelper.getInstance().getTable(column.getType()));
                if (column.isForeignKey()) {
                    content.append(" integer");
                }
            } else if (column.isList()) {
                content.append(" text");
            } else {
                content.append(" ");
                content.append(column.getType());
            }

            if (column.isPrimary()) {
                content.append(" primary key");
            }
            content.append(",");
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
        for (Table table : SchemaHelper.getInstance().getDatabase().getTables()) {
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
    public void emptyTables() {
        for (Table table : SchemaHelper.getInstance().getDatabase().getTables()) {
            emptyTable(table);
        }
    }

    /**
     * delete every row of the table represented by the object passed as parameter
     */
    public void emptyTable(Table table) {
        String request = "DELEtE FROM " + table.getName();
        db.execute(request);
    }

}
