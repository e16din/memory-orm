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

@RunWith(AndroidJUnit4.class)
public class DbManagerTest {

    private static final String DB_NAME = "DbTest";
    private static final String TABLE_NAME = "Test";

    private static  Context context;
    private static DbManager manager;

    @BeforeClass
    public static void classSetUp() {
        context = InstrumentationRegistry.getContext();
        manager = new DbManager(context, DB_NAME, null, 1);
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
        manager.execute("create table " + TABLE_NAME + " (id integer primary key, name text)");
    }

    @Test
    public void shouldQuery() throws Exception {
        Cursor cursor = manager.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + TABLE_NAME  +"'", null);
        Assert.assertEquals(1, cursor.getCount());
    }

    @Test
    public void shouldInsert() throws Exception {
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("name", "John Doe");
        long rowId = manager.insert(TABLE_NAME, values);
        Assert.assertEquals(1, rowId);
    }

}
