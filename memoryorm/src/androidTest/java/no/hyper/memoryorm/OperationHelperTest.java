package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.hyper.memoryorm.model.Database;

/**
 * Created by jean on 13.06.2016.
 */
public class OperationHelperTest {

    private static final String DB_NAME = "DbTest";

    private static Context context;
    private static DbManager manager;
    private static TableHelper tableHelper;
    private static OperationHelper operationHelper;

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
        manager = new DbManager(context, DB_NAME, null, 1);
        tableHelper = new TableHelper(manager);
        operationHelper = new OperationHelper(manager);
        manager.openDb();
    }

    @After
    public void finish() {
        manager.closeDb();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void shouldInsert() {
        tableHelper.createTables();
        long id = operationHelper.insert(getGroup(0), null);

        Assert.assertEquals(1, id);

        Cursor cursor = manager.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(1, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(3, cursor.getCount());
    }

    @Test
    public void shouldInsertList() {
        tableHelper.createTables();
        List<PersonGroup> groups = new ArrayList<>();
        groups.add(getGroup(0));
        groups.add(getGroup(5));
        groups.add(getGroup(10));
        List<Long> ids = operationHelper.insertList(groups, null);

        for (int i = 0; i < ids.size(); i++) {
            Assert.assertEquals(Long.valueOf(i+1), ids.get(i));
        }

        Cursor cursor = manager.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(3, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(9, cursor.getCount());
    }

    @Test
    public void shouldFetchAll() {
        tableHelper.createTables();
        List<PersonGroup> groups = new ArrayList<>();
        groups.add(getGroup(0));
        groups.add(getGroup(5));
        groups.add(getGroup(10));
        List<Long> ids = operationHelper.insertList(groups, null);

        for (int i = 0; i < ids.size(); i++) {
            Assert.assertEquals(Long.valueOf(i+1), ids.get(i));
        }

        Cursor cursor = manager.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(3, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(9, cursor.getCount());

        List<PersonGroup> fetched = operationHelper.fetchAll(PersonGroup.class, null);
        Assert.assertEquals(3, fetched.size());
    }

    private PersonGroup getGroup(int i) {
        Person chef = new Person("idchef" + i, "chef" + i, 50, true);
        List<Person> members = new ArrayList<>();
        members.add(new Person("member" + i, "member" + i, 23, true));
        members.add(new Person("member" + (i+1), "member"  + (i+1), 23, true));
        List<String> departments = new ArrayList<>();
        departments.add("dep" + i);
        departments.add("dep" + (i+1));
        List<Integer> codes = new ArrayList<>();
        codes.add(1234);
        codes.add(5678);

        PersonGroup group = new PersonGroup("group" + i, "group" + i, chef, members, departments, codes);
        return group;
    }

}
