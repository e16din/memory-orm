package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.helper.DatabaseHelper;
import no.hyper.memoryorm.helper.OperationHelper;

/**
 * Created by Jean on 5/12/2016.
 */
public class Memory {

    private static final String LOG_TAG = Memory.class.getSimpleName();
    private DbManager db;
    private DatabaseHelper tableHelper;
    private OperationHelper operationHelper;
    private Context context;

    public Memory(Context context) {
        this.context = context;
        db = DbManager.getInstance(context, context.getPackageName(), null, 1);

        tableHelper = new DatabaseHelper(db, context);
        operationHelper = new OperationHelper(db);
    }

    /**
     * create the database
     */
    public void createDatabase() {
        db.openDb();
        tableHelper.createTables();
        db.closeDb();
    }

    /**
     * delete the database
     */
    public void deleteDatabase() {
        context.deleteDatabase(context.getPackageName());
    }

    /**
     * delete every row from all the tables
     */
    public void emptyDatabase() {
        db.openDb();
        tableHelper.cleanTables();
        db.closeDb();
    }

    /**
     * delete every row from one tables
     * @param tableName the name of the table to clean
     * @param clause a clause to specify which rows need to be erased
     */
    public void emptyTable(String tableName, String clause) {
        db.openDb();
        tableHelper.cleanTable(tableName, clause);
        db.closeDb();
    }

    /**
     * write the object's attributes in the table that have the same name
     * @param entity the object to save
     * @param <T> the type of the object to save
     * @return return the rowId or null if the operation failed
     */
    public <T> long save(T entity) {
        db.openDb();
        long result = operationHelper.insert(context, entity, null);
        db.closeDb();
        return result;
    }

    /**
     * save a list of object in their corresponding database
     * @param list the list of item to save
     * @param <T>
     * @return if it succeed, the list of rowids is returned, null otherwise
     */
    public <T> List<Long> save(List<T> list) {
        if (list.size() <= 0) return null;
        db.openDb();
        List<Long> rows = operationHelper.insert(context, list, null);
        db.closeDb();
        return rows;
    }

    /**
     * return from the database a list of object contained in a table
     * @param classType the type of the object corresponding to a table in the database
     * @param <T>
     * @return all the rows from the table
     */
    public <T> List<T>  fetchAll(Class<T> classType) {
        db.openDb();
        List<T> result = operationHelper.fetchAll(context, classType, null);
        db.closeDb();
        return result;
    }

    /**
     * return from the database the first object contained in a table
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param <T>
     * @return a single object from the table
     */
    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.openDb();
        T entity = operationHelper.fetchFirst(context, entityToFetch, null);
        db.closeDb();
        return entity;
    }

    /**
     * return from the database the object with the specified id
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param id the id to look for
     * @param <T>
     * @return a single object from the table
     */
    public <T> T fetchById (Class<T> entityToFetch, String id) {
        db.openDb();
        T result = operationHelper.fetchById(context, entityToFetch, id);
        db.closeDb();
        return result;
    }

    /**
     * return from the database the object with the specified row id
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param rowId the row id to look for
     * @param <T>
     * @return a single object from the table
     */
    public <T> T fetchByRowId (Class<T> entityToFetch, Long rowId) {
        db.openDb();
        T result = operationHelper.fetchByRowId(context, entityToFetch, rowId);
        db.closeDb();
        return result;
    }

    /**
     * update the row in database represented by an object
     * @param entity the object to update
     * @param <T>
     * @return true if it worked, false otherwise
     */
    public <T> long update(T entity) {
        db.openDb();
        long result = operationHelper.update(context, entity);
        db.closeDb();
        return result;
    }

    /**
     * update the rows in database represented by the objects
     * @param list list of entity to update
     * @param <T>
     * @return list of boolean, one for each update, in the same order
     */
    public <T> List<Long> update(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            ids.add(operationHelper.update(context, entity));
        }
        db.closeDb();
        return ids;
    }

    /**
     * if the object does not exist in database, it will be inserted, or update otherwise
     * @param entity entity to either save or update
     * @param <T>
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted
     */
    public <T> long saveOrUpdate(T entity) {
        db.openDb();
        long result = operationHelper.saveOrUpdate(context, entity);
        db.closeDb();
        return result;
    }

    /**
     * execute the function `saveOrUpdate` for each items of the list
     * @param list list of item to save or update
     * @param <T>
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted, for each items
     */
    public <T> List<Long> saveOrUpdate(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            ids.add(operationHelper.saveOrUpdate(context, entity));
        }
        db.closeDb();
        return ids;
    }

    public void openDb() {
        this.db.openDb();
    }

    public void closeDb() {
        this.db.closeDb();
    }

    /**
     * execute a raw query in the database. You need to open the database first by using `openDb`
     * @param query the query to execute
     * @param args the args to pass that will replace the `?` inside your query
     * @return a cursor containing the result of the query
     */
    public Cursor rawQuery(String query, String[] args) {
        return db.rawQuery(query, args);
    }

}
