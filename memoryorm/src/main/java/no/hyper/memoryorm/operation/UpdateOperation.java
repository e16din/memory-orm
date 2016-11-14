package no.hyper.memoryorm.operation;

import android.content.Context;
import android.database.Cursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.DbManager;
import no.hyper.memoryorm.builder.QueryBuilder;
import no.hyper.memoryorm.helper.ObjectHelper;

/**
 * Created by jean on 14.11.2016.
 */

public class UpdateOperation {

    /**
     * update the row corresponding to the entity passed by argument
     * @param context the android context
     * @param entity the object to update
     * @param <T> type of the entity to fetch
     * @return the number of row affected
     */
    public static <T> long update(DbManager db, Context context, T entity) {
        String id = null;
        try {
            id = ObjectHelper.getEntityId(entity);
            return update(db, context, entity, id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Save the entity in db if it does not exist or update it otherwise.
     * @param context the android context
     * @param entity the object to save or update
     * @param <T> type of the entity to fetch
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted
     */
    public static <T> long saveOrUpdate(DbManager db, Context context, T entity)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        String id = ObjectHelper.getEntityId(entity);
        if (!id.equals("-1")) {
            String query = getFetchByIdRequest(entity.getClass().getSimpleName(), id);
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.getCount() > 0) {
                update(db, context, entity, id);
                return 0;
            } else {
                return InsertOperation.insert(db, context, entity, null);
            }
        } else {
            return -1;
        }
    }

    /**
     * for each item in the list, it save it in db if it does not exist, or update it otherwise.
     * @param context the android context
     * @param list the list of object to save or update
     * @param <T> type of the entity to fetch
     * @return for each items, -1 if it failed, 0 if a row was updated or the rowid if it inserted
     */
    public static <T> List<Long> saveOrUpdate(DbManager db, Context context, List<T> list)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        if (list.size() <= 0) return null;
        List<Long> results = new ArrayList<>();
        for(T entity : list) {
            results.add(saveOrUpdate(db, context, entity));
        }
        return results;
    }

    private static <T> long update(DbManager db, Context context, T entity, String id) throws IOException {
        return db.update(entity.getClass().getSimpleName(), ObjectHelper.getEntityContentValues(context, entity), id);
    }

    private static String getFetchByIdRequest(String table, String id) {
        return new QueryBuilder()
                .select()
                .from(table)
                .where("id=" + id)
                .toSqlRequest();
    }

}
