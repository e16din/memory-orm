package no.hyper.memoryorm.Helper;

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

    public String getDatabase(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("schema/database.json");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();

            String line;
            do {
                line = reader.readLine();
                if (line != null) sb.append(line);
            } while (line != null);

            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
