package no.hyper.memoryorm;

import com.google.gson.Gson;

import no.hyper.memoryorm.model.Database;
import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 17.06.2016.
 */
public class SchemaHelper {

    private static final String jsonSchema = "{\"tables\":[{\"name\":\"Person\",\"columns\":[{\"label\":\"id\",\"type\":\"text\",\"primary\":true},{\"label\":\"name\",\"type\":\"text\"},{\"label\":\"age\",\"type\":\"integer\"},{\"label\":\"active\",\"type\":\"integer\"},{\"label\":\"id_PersonGroup\",\"type\":\"integer\"}]},{\"name\":\"PersonGroup\",\"columns\":[{\"label\":\"id\",\"type\":\"text\",\"primary\":true},{\"label\":\"name\",\"type\":\"text\"},{\"label\":\"chef\",\"type\":\"Person\"},{\"label\":\"departments\",\"list\":true,\"type\":\"text\"},{\"label\":\"members\",\"list\":true,\"type\":\"Person\"},{\"label\":\"codes\",\"list\":true,\"type\":\"integer\"}]}]}";
    private static SchemaHelper instance;
    private Gson gson = new Gson();
    private Database dbSchema;

    public static SchemaHelper getInstance() {
        if (instance == null) {
            instance = new SchemaHelper();
        }

        return instance;
    }

    public Database getDatabase() {
        if (dbSchema == null) {
            dbSchema = gson.fromJson(jsonSchema, Database.class);
        }
        return dbSchema;
    }

    public Table getTable(String name) {
        for (Table table : getDatabase().getTables()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

}
