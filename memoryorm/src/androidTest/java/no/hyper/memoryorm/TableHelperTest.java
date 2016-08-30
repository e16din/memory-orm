package no.hyper.memoryorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import no.hyper.memoryorm.Helper.TableHelper;

/**
 * Created by Jean on 5/31/2016.
 */
public class TableHelperTest {

    private static final String DB_NAME = "DbTest";
    private static final String JSON_DB = "{\"tables\":[{\"name\":\"Person\",\"columns\":[{\"label\":\"id\",\"type\":\"text\",\"primary\":true},{\"label\":\"name\",\"type\":\"text\"},{\"label\":\"age\",\"type\":\"integer\"},{\"label\":\"active\",\"type\":\"integer\"},{\"label\":\"id_PersonGroup\",\"type\":\"integer\"}]},{\"name\":\"PersonGroup\",\"columns\":[{\"label\":\"id\",\"type\":\"text\",\"primary\":true},{\"label\":\"name\",\"type\":\"text\"},{\"label\":\"chef\",\"type\":\"Person\"},{\"label\":\"departments\",\"list\":true,\"type\":\"text\"},{\"label\":\"members\",\"list\":true,\"type\":\"Person\"},{\"label\":\"codes\",\"list\":true,\"type\":\"integer\"}]}]}";

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

    @Before
    public void start() {
        context = InstrumentationRegistry.getContext();
        manager = DbManager.getInstance(context, DB_NAME, null, 1);
        tableHelper = new TableHelper(manager, JSON_DB);
        manager.openDb();
    }

    @After
    public void finish() {
        manager.closeDb();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void shouldCreateTables() throws Exception {
        tableHelper.createTables();
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(PersonGroup.class.getSimpleName()));
    }

    @Test
    public void shouldDeleteTable() throws Exception {
        tableHelper.createTables();
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(PersonGroup.class.getSimpleName()));

        tableHelper.deleteTables();
        Assert.assertFalse(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertFalse(checkIfTableExist(PersonGroup.class.getSimpleName()));
    }

    @Test
    public void shouldEmptyTable() throws Exception {
        tableHelper.createTables();
        Assert.assertTrue(checkIfTableExist(Person.class.getSimpleName()));
        Assert.assertTrue(checkIfTableExist(PersonGroup.class.getSimpleName()));

        ContentValues values = new ContentValues();
        values.put("id", "personId");
        values.put("name", "toto");
        values.put("age", 11);
        values.put("active", true);
        manager.insert(Person.class.getSimpleName(), values);

        Cursor cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(1, cursor.getCount());

        tableHelper.cleanTables();
        Cursor cursor2 = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(0, cursor2.getCount());
    }

    private boolean checkIfTableExist(String tableName) {
        Cursor cursor = manager.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + Person.class.getSimpleName()  +"'", null);
        return cursor.getCount() == 1;
    }

}
