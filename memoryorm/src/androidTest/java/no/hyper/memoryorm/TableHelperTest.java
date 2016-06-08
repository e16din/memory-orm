package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by Jean on 5/31/2016.
 */
public class TableHelperTest {

    private static final String DB_NAME = "DbTest";

    private static Context context;
    private static DbManager manager;
    private static TableHelper tableHelper;

    private class Person {

        private String id;
        private String name;
        private int age;
        private boolean active;

        public  Person(String id, String name, int age, boolean active) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.active = active;
        }

    }

    private class Group {

        public String id;
        public String name;
        public Person chef;
        public List<Person> members;

        public Group(String id, String name, Person chef, List<Person> members) {
            this.id = id;
            this.name = name;
            this.chef = chef;
            this.members = members;
        }

    }

    @BeforeClass
    public static void classSetUp() {
        context = InstrumentationRegistry.getContext();
        manager = new DbManager(context, DB_NAME, null, 1);
        tableHelper = new TableHelper(manager);
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
    public void shouldGetCreateTableRequest() {
        String personRequest = tableHelper.getSqlTableCreationRequest(Person.class, null);
        Assert.assertEquals("CREATE TABLE IF NOT EXISTS Person(id TEXT PRIMARY KEY,name TEXT,active INTEGER," +
                "age INTEGER);", personRequest);
    }

    @Test
    public void shouldCreateTable() {
        tableHelper.createTableFrom(Person.class);
        Cursor cursor = manager.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + Person.class.getSimpleName()  +"'", null);
        Assert.assertEquals(1, cursor.getCount());
    }

}
