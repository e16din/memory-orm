package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.helper.DatabaseHelper;
import no.hyper.memoryorm.operation.FetchOperation;
import no.hyper.memoryorm.operation.InsertOperation;
import no.hyper.memoryorm.operation.UpdateOperation;

/**
 * Created by Jean on 5/12/2016.
 */
public class Memory {

    private DbManager db;
    private DatabaseHelper databaseHelper;
    private Context context;

    public Memory(Context context) {
        this.context = context;
        db = DbManager.getInstance(context, context.getPackageName(), null, 1);

        databaseHelper = new DatabaseHelper(db, context);
    }

    /**
     * create the database
     */
    public void createDatabase() {
        db.openDb();
        try {
            databaseHelper.createTables();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.closeDb();
    }

    /**
     * delete the database
     */
    public void deleteDatabase() {
        context.deleteDatabase(context.getPackageName());
    }

    /**
     * write the object's attributes in the table that have the same name
     * @param entity the object to save
     * @param <T> the type of the object to save
     * @return return the rowId or null if the operation failed
     */
    public <T> Long save(T entity) {
        db.openDb();
        long result = 0;
        try {
            result = InsertOperation.insert(db, context, entity, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * save a list of object in their corresponding database
     * @param list the list of item to save
     * @param <T> the type of the object to save
     * @return if it succeed, the list of rowids is returned, null otherwise
     */
    public <T> List<Long> save(List<T> list) {
        if (list.size() <= 0) return null;
        db.openDb();
        List<Long> rows = null;
        try {
            rows = InsertOperation.insert(db, context, list, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return rows;
    }

    /**
     * if the object does not exist in database, it will be inserted, or update otherwise
     * @param entity entity to either save or update
     * @param <T> the type of the object to save or update
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted
     */
    public <T> Long saveOrUpdate(T entity) {
        db.openDb();
        long result = 0;
        try {
            result = UpdateOperation.saveOrUpdate(db, context, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * execute the function `saveOrUpdate` for each items of the list
     * @param list list of item to save or update
     * @param <T> the type of the object to save or update
     * @return -1 if it failed, 0 if it updated a row or the rowid if it inserted, for each items
     */
    public <T> List<Long> saveOrUpdate(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            try {
                ids.add(UpdateOperation.saveOrUpdate(db, context, entity));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.closeDb();
        return ids;
    }

    /**
     * update the row in database represented by an object
     * @param entity the object to update
     * @param <T> the type of the object to update
     * @return true if it worked, false otherwise
     */
    public <T> Long update(T entity) {
        db.openDb();
        long result = 0;
        try {
            result = UpdateOperation.update(db, context, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * update the rows in database represented by the objects
     * @param list list of entity to update
     * @param <T> the type of the object to update
     * @return list of boolean, one for each update, in the same order
     */
    public <T> List<Long> update(List<T> list) {
        db.openDb();
        List<Long> ids = new ArrayList<>();
        for(T entity : list) {
            try {
                ids.add(UpdateOperation.update(db, context, entity));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.closeDb();
        return ids;
    }

    /**
     * return from the database a list of object contained in a table
     * @param classType the type of the object corresponding to a table in the database
     * @param <T> the type of the object to fetch
     * @return all the rows from the table
     */
    public <T> List<T>  fetchAll(Class<T> classType) {
        db.openDb();
        List<T> result = null;
        try {
            result = FetchOperation.fetchAll(db, context, classType, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * return from the database the first object contained in a table
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param <T> the type of the object to fetch
     * @return a single object from the table
     */
    public <T> T fetchFirst(Class<T> entityToFetch) {
        db.openDb();
        T entity = null;
        try {
            entity = FetchOperation.fetchFirst(db, context, entityToFetch, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return entity;
    }

    /**
     * return from the database the object with the specified id
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param id the id to look for
     * @param <T> the type of the object to fetch
     * @return a single object from the table
     */
    public <T> T fetchById (Class<T> entityToFetch, String id) {
        db.openDb();
        T result = null;
        try {
            result = FetchOperation.fetchById(db, context, entityToFetch, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * return from the database the object with the specified row id
     * @param entityToFetch the type of the object corresponding to a table in the database
     * @param rowId the row id to look for
     * @param <T> the type of the object to fetch
     * @return a single object from the table
     */
    public <T> T fetchByRowId (Class<T> entityToFetch, Long rowId) {
        db.openDb();
        T result = null;
        try {
            result = FetchOperation.fetchByRowId(db, context, entityToFetch, rowId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.closeDb();
        return result;
    }

    /**
     * count the number of row in a table
     * @param tableName name of the table that is counted
     * @return the number of row in a table
     */
    public Integer getTableCount(String tableName) {
        db.openDb();
        Integer rows = FetchOperation.getTableCount(db, tableName);
        db.closeDb();
        return rows;
    }

    /**
     * delete every row from all the tables
     */
    public void emptyDatabase() {
        db.openDb();
        try {
            databaseHelper.cleanTables();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.closeDb();
    }

    /**
     * delete every row from one tables
     * @param tableName the name of the table to clean
     * @param clause a clause to specify which rows need to be erased
     */
    public void emptyTable(String tableName, String clause) {
        db.openDb();
        databaseHelper.cleanTable(tableName, clause);
        db.closeDb();
    }

    public void deleteById(String tableName, String id) {
        db.openDb();
        db.delete(tableName, "id='" + id + "';");
        db.closeDb();
    }

    Cursor rawQuery(String query, String[] args) {
        return db.rawQuery(query, args);
    }

    void openDb() {
        this.db.openDb();
    }

    void closeDb() {
        this.db.closeDb();
    }

}
