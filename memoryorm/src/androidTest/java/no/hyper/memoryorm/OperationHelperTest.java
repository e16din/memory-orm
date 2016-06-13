package no.hyper.memoryorm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by jean on 13.06.2016.
 */
public class OperationHelperTest {

    private static final String DB_NAME = "DbTest";

    private static Context context;
    private static DbManager manager;
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
        long id = operationHelper.insert(new Person("id", "toto", 25, true));
        Assert.assertEquals(1, id);
    }

}
