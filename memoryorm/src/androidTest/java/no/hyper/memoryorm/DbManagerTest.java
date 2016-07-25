package no.hyper.memoryorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by Jean on 5/29/2016.
 */

public class DbManagerTest {

    private static final String DB_NAME = "DbTest";
    private static final String TABLE_NAME = "Test";

    private static  Context context;
    private static DbManager manager;

    @BeforeClass
    public static void classSetUp() {
        context = InstrumentationRegistry.getContext();
        manager = new DbManager(context, DB_NAME, null, 1);
        manager.execute("create table " + TABLE_NAME + " (id integer primary key, name text)");
    }

    @AfterClass
    public static void classCleanUp() {
        context.deleteDatabase(DB_NAME);
    }

    @Before
    public void start() {
        manager.openDb();
    }

    @After
    public void finish() {
        manager.closeDb();
    }

    @Test
    public void shouldOpenDb() throws Exception {
        Assert.assertEquals(true, manager.isDbOpen());
    }

    @Test
    public void shouldExecute() throws Exception {
        manager.execute("delete form " + TABLE_NAME);
    }

    @Test
    public void shouldRawQuery() throws Exception {
        Cursor cursor = manager.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + TABLE_NAME  +"'", null);
        Assert.assertEquals(1, cursor.getCount());
    }

    @Test
    public void shouldInsert() throws Exception {
        Assert.assertTrue(insert(1) > 0);
    }

    @Test
    public void shouldUpdate() throws Exception {
        long rowId = insert(2);
        ContentValues values = new ContentValues();
        values.put("name", "John Doe2");
        long rowAffected = manager.update(TABLE_NAME, values, String.valueOf(rowId));
        Assert.assertEquals(1, rowAffected);

        Cursor cursor = manager.rawQuery("SELECT name FROM " + TABLE_NAME  +" where id='" + rowId + "'", null);
        Assert.assertEquals(1, cursor.getCount());

        cursor.moveToPosition(0);
        int index = cursor.getColumnIndex("name");
        String name = cursor.getString(index);
        Assert.assertEquals("John Doe2", name);
    }

    private long insert(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", "John Doe");
        return manager.insert(TABLE_NAME, values);
    }

}
