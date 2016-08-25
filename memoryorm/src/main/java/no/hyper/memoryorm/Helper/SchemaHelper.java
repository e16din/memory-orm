package no.hyper.memoryorm.Helper;

import com.google.gson.Gson;

import no.hyper.memoryorm.model.Database;
import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 17.06.2016.
 */
public class SchemaHelper {

    private static SchemaHelper instance;
    private Gson gson = new Gson();
    private Database dbSchema;

    public static SchemaHelper getInstance() {
        if (instance == null) {
            instance = new SchemaHelper();
        }

        return instance;
    }

    public Database getDatabase(String jsonDb) {
        if (dbSchema == null) {
            dbSchema = gson.fromJson(jsonDb, Database.class);
        }
        return dbSchema;
    }

    public Table getTable(String jsonDb, String name) {
        for (Table table : getDatabase(jsonDb).getTables()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

}
