package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean on 5/12/2016.
 */
public class Memory {

    private static final String LOG_TAG = Memory.class.getSimpleName();
    private DbManager db;
    private TableHelper tableHelper;
    private OperationHelper operationHelper;
    private String jsonDb;

    public Memory(Context context, String jsonDb) {
        this.jsonDb = jsonDb;
        db = new DbManager(context, context.getPackageName(), null, 1);
        tableHelper = new TableHelper(db, jsonDb);
        operationHelper = new OperationHelper(db);
    }

    /**
     * create the database
     */
    public void createTables() {
        db.openDb();
        tableHelper.createTables();
        db.closeDb();
    }

    /**
     * delete every row from all the tables
     */
    public void cleanTables() {
        db.openDb();
        tableHelper.cleanTables();
        db.closeDb();
    }

    /**
     * save an object in its corresponding table
     * @param entity the object to save
     * @param <T>
     * @return if it succeed, the rowid is returned, null otherwise
     */
    public <T> long save(T entity) {
        db.openDb();
        long result = operationHelper.insert(jsonDb, entity, null);
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
        List<Long> rows = operationHelper.insert(jsonDb, list, null);
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
        List<T> result = operationHelper.fetchAll(jsonDb, classType, null);
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
        T entity = operationHelper.fetchFirst(jsonDb, entityToFetch, null);
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
        T result = operationHelper.fetchById(jsonDb, entityToFetch, id);
        db.closeDb();
        return result;
    }

    /**
     * update the row in database represented by an object
     * @param entity the object to update
     * @param <T>
     * @return true if it worked, false otherwise
     */
    public <T> boolean update(T entity) {
        db.openDb();
        boolean result = operationHelper.update(jsonDb, entity);
        db.closeDb();
        return result;
    }

    /**
     * update the rows in database represented by the objects
     * @param list list of entity to update
     * @param <T>
     * @return list of boolean, one for each update, in the same order
     */
    public <T> List<Boolean> update(List<T> list) {
        db.openDb();
        List<Boolean> ids = new ArrayList<>();
        for(T entity : list) {
            ids.add(operationHelper.update(jsonDb, entity));
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
        long result = operationHelper.saveOrUpdate(jsonDb, entity);
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
            ids.add(operationHelper.saveOrUpdate(jsonDb, entity));
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
