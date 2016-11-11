package no.hyper.memoryorm.helper;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.hyper.memoryorm.model.Database;
import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 17.06.2016.
 */
public class SchemaHelper {

    private static final String PATH = "schema/database.json";
    private static SchemaHelper instance;
    private Database db;

    public static SchemaHelper getInstance() {
        if (instance == null) {
            instance = new SchemaHelper();
        }
        return instance;
    }

    public Database getDatabase(Context context) throws IOException {
        if (db == null) {
            InputStream stream = context.getAssets().open(PATH);
            String schema = getJsonSchema(stream);
            Gson gson = new Gson();
            db = gson.fromJson(schema, Database.class);
        }
        return db;
    }

    public Table getTable(Context context, String name) throws IOException {
        for (Table table : getDatabase(context).getTables()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    private String getJsonSchema(InputStream stream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();

        String line;
        do {
            line = reader.readLine();
            if (line != null) sb.append(line);
        } while (line != null);

        reader.close();
        return sb.toString();
    }

}
