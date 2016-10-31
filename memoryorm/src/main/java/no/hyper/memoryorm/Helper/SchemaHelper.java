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
    private static Database db;

    public static Database getDatabase(Context context) {
        if (db == null) {
            String schema = getJsonSchema(context);
            Gson gson = new Gson();
            db = gson.fromJson(schema, Database.class);
        }
        return db;
    }

    public static Table getTable(Context context, String name) {
        for (Table table : getDatabase(context).getTables()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    private static String getJsonSchema(Context context) {
        if (context == null) return null;
        try {
            InputStream inputStream = context.getAssets().open(PATH);
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
