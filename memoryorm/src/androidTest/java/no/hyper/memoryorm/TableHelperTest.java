package no.hyper.memoryorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
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

    private class PersonGroup {

        public String id;
        public String name;
        public Person chef;
        public List<Person> members;
        public List<String> departments;
        public List<Integer> codes;

        public PersonGroup(String id, String name, Person chef, List<Person> members, List<String> departments,
                           List<Integer> codes) {
            this.id = id;
            this.name = name;
            this.chef = chef;
            this.members = members;
            this.departments = departments;
            this.codes = codes;
        }

    }

    private static final String PERSON_SQL_CREATION_TABLE = "CREATE TABLE IF NOT EXISTS Person(id TEXT PRIMARY KEY," +
            "name TEXT,active INTEGER,age INTEGER);";

    @Before
    public void start() {
        context = InstrumentationRegistry.getContext();
        manager = new DbManager(context, DB_NAME, null, 1);
        tableHelper = new TableHelper(manager);
        manager.openDb();
    }

    @After
    public void finish() {
        manager.closeDb();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void shouldGetSqlTableCreationRequest() {
        String personRequest = tableHelper.getSqlTableCreationRequest(Person.class, null);
        Assert.assertEquals(PERSON_SQL_CREATION_TABLE, personRequest);
    }

    @Test
    public void shouldCreateManyToOneRelationTable() {
        for (Field field : ObjectHelper.getDeclaredFields(PersonGroup.class)) {
            if (ObjectHelper.isAList(field)) {
                tableHelper.createManyToOneRelationTable(PersonGroup.class, field);
            }
        }
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(String.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(Integer.class.getSimpleName()));
    }

    @Test
    public void shouldCreateTable() {
        tableHelper.createTableFrom(PersonGroup.class);
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(String.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(Integer.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(PersonGroup.class.getSimpleName()));
    }

    @Test
    public void shouldDeleteTable() {
        tableHelper.createTableFrom(PersonGroup.class);
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(String.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(Integer.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(PersonGroup.class.getSimpleName()));

        tableHelper.deleteTable(PersonGroup.class);
        Assert.assertFalse(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertFalse(checkIfTableExist(String.class.getSimpleName()));
        Assert.assertFalse(checkIfTableExist(Integer.class.getSimpleName()));
        Assert.assertFalse(checkIfTableExist(PersonGroup.class.getSimpleName()));
    }

    @Test
    public void shouldEmptyTable() {
        tableHelper.createTableFrom(Person.class);
        ContentValues values = new ContentValues();
        values.put("id", "personId");
        values.put("name", "toto");
        values.put("age", 11);
        values.put("active", true);
        manager.insert(Person.class.getSimpleName(), values);

        Cursor cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(1, cursor.getCount());

        tableHelper.emptyTable(Person.class);
        Cursor cursor2 = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(0, cursor2.getCount());
    }

    private boolean checkIfTableExist(String tableName) {
        Cursor cursor = manager.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + Person.class.getSimpleName()  +"'", null);
        return cursor.getCount() == 1;
    }

}
