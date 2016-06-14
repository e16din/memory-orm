package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jean on 13.06.2016.
 */
public class OperationHelperTest {

    private static final String DB_NAME = "DbTest";
    private static int i = 0;

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
        tableHelper.createTableFrom(PersonGroup.class);
        long id = operationHelper.insert(getGroup());

        Assert.assertEquals(1, id);

        Cursor cursor = manager.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(1, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(3, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM String", null);
        Assert.assertEquals(2, cursor.getCount());

        cursor = manager.rawQuery("SELECT * FROM Integer", null);
        Assert.assertEquals(2, cursor.getCount());
    }

    @Test
    public void shouldInsertList() {
        tableHelper.createTableFrom(PersonGroup.class);
        List<PersonGroup> groups = new ArrayList<>();
        groups.add(getGroup());
        groups.add(getGroup());
        groups.add(getGroup());
        List<Long> ids = operationHelper.insertList(groups);

        Assert.assertEquals(3, ids.size());
    }

    @Test
    public <T> void shouldFetchNestedList() {
        tableHelper.createTableFrom(PersonGroup.class);
        List<PersonGroup> groups = new ArrayList<>();
        groups.add(getGroup());
        groups.add(getGroup());
        groups.add(getGroup());
        List<Long> ids = operationHelper.insertList(groups);
        Assert.assertEquals(3, ids.size());

        HashMap<String, Object> mapNestedObjects = new HashMap<>();
        for (Field field : ObjectHelper.getDeclaredFields(PersonGroup.class)) {
            if (ObjectHelper.isAList(field)) {
                Class<T> listType = ObjectHelper.getActualListType(field);
                List<T> list = operationHelper.fetchNestedList(PersonGroup.class, listType, 1);
                mapNestedObjects.put(field.getName(), list);
            }
        }
        Assert.assertEquals(3, mapNestedObjects.size());
    }

    private PersonGroup getGroup() {
        Person chef = new Person("idchef", "chef", 50, true);
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
        i++;
        return group;
    }

}
