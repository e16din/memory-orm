package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import no.hyper.memoryorm.model.Table;

/**
 * Created by jean on 25.07.2016.
 */
public class MemoryTest {

    private static final String DB_NAME = "DbTest";

    private Memory memory;
    private Context context;

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
    public void start() throws Exception {
        context = InstrumentationRegistry.getContext();
        memory = new Memory(context);
    }

    @After
    public void finish() throws Exception {
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void shouldCreateDatabase() throws Exception {
        memory.createTables();
        Table personTable = SchemaHelper.getInstance().getTable(Person.class.getSimpleName());
        Table personGroupTable = SchemaHelper.getInstance().getTable(PersonGroup.class.getSimpleName());

        Assert.assertTrue(personTable != null && personGroupTable != null);
    }

    @Test
    public void shouldSave() {
        memory.save(new Person("id", "test", 23, true));

        memory.openDb();
        Cursor cursor = memory.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(1, cursor.getCount());
        memory.closeDb();
    }

    @Test
    public void shouldSaveList() {
        memory.save(getGroups());

        memory.openDb();
        Cursor cursor = memory.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(3, cursor.getCount());

        cursor = memory.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(9, cursor.getCount());
        memory.closeDb();
    }


    @Test
    public void shouldCleanDatabase() {
        memory.save(getGroups());

        memory.openDb();
        Cursor cursor = memory.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(3, cursor.getCount());

        cursor = memory.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(9, cursor.getCount());
        memory.closeDb();

        memory.cleanTables();

        memory.openDb();
        cursor = memory.rawQuery("SELECT * FROM Person", null);
        Assert.assertEquals(0, cursor.getCount());
        cursor = memory.rawQuery("SELECT * FROM PersonGroup", null);
        Assert.assertEquals(0, cursor.getCount());
        memory.closeDb();
    }

    private List<PersonGroup> getGroups() {
        Person chef0 = new Person("idchef0", "chef0", 50, true);
        Person chef1 = new Person("idchef1", "chef1", 51, true);
        Person chef2 = new Person("idchef2", "chef2", 52, true);

        List<Person> members0 = new ArrayList<>();
        members0.add(new Person("member0", "member0", 23, true));
        members0.add(new Person("member1", "member1", 23, true));
        List<Person> members1 = new ArrayList<>();
        members1.add(new Person("member2", "member2", 23, true));
        members1.add(new Person("member3", "member3", 23, true));
        List<Person> members2 = new ArrayList<>();
        members2.add(new Person("membe4r", "member4", 23, true));
        members2.add(new Person("member5", "member5", 23, true));

        List<String> departments0 = new ArrayList<>();
        departments0.add("dep0");
        departments0.add("dep1");
        List<String> departments1 = new ArrayList<>();
        departments1.add("dep2");
        departments1.add("dep3");
        List<String> departments2 = new ArrayList<>();
        departments2.add("dep4");
        departments2.add("dep5");

        List<Integer> codes0 = new ArrayList<>();
        codes0.add(12);
        codes0.add(34);
        List<Integer> codes1 = new ArrayList<>();
        codes1.add(56);
        codes1.add(78);
        List<Integer> codes2 = new ArrayList<>();
        codes2.add(91);
        codes2.add(23);

        List<PersonGroup> groups = new ArrayList<>();
        groups.add(new PersonGroup("group0", "group0", chef0, members0, departments0, codes0));
        groups.add(new PersonGroup("group1", "group1", chef1, members1, departments1, codes1));
        groups.add(new PersonGroup("group2", "group2", chef2, members2, departments2, codes2));

        return groups;
    }

}
