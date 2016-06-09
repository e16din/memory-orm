package no.hyper.memoryorm;

import android.content.Context;
import android.database.Cursor;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by jean on 01.06.2016.
 */
public class EntityBuilderTest {

    @Mock
    Context mMockContext;

    private class Person {

        public String id;
        public String name;
        public int age;
        public boolean active;

        public Person (String id, String name, int age, boolean active) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.active  = active;
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

    @Test
    public void shouldGetDefaultConstructorParameters() throws Exception {
        Assert.assertNotNull(getDefaultInstance(Person.class));
        Assert.assertNotNull(getDefaultInstance(Group.class));
    }

    @Test
    public void shouldBindHashMapToEntity() throws Exception {
        Person person = getDefaultInstance(Person.class);
        HashMap<String, Object> values = getHashMapValues(true);
        person = EntityBuilder.bindHashMapToEntity(person, values);

        Assert.assertEquals(person.id, values.get("id"));
        Assert.assertEquals(person.name, values.get("name"));
        Assert.assertEquals(person.age, values.get("age"));
        Assert.assertEquals(person.active, values.get("active"));

        Group group = getDefaultInstance(Group.class);
        HashMap<String, Object> valuesGroup = getHashMapGroupValues();
        group = EntityBuilder.bindHashMapToEntity(group, valuesGroup);

        Assert.assertEquals(group.id, valuesGroup.get("id"));
        Assert.assertEquals(group.name, valuesGroup.get("name"));
        Assert.assertEquals(group.chef, valuesGroup.get("chef"));
        Assert.assertEquals(group.members, valuesGroup.get("members"));
    }

    @Test
    public void shouldBindCursorToHashMap() throws Exception {
        HashMap<String, Object> values = getHashMapValues(false);
        Cursor cursor = mockCursorObjectForPerson(values);
        HashMap<String, Object> map = EntityBuilder.bindCursorToHashMap(Person.class, cursor);

        Assert.assertEquals(values.get("id"), map.get("id"));
        Assert.assertEquals(values.get("name"), map.get("name"));
        Assert.assertEquals(values.get("age"), map.get("age"));
        Assert.assertEquals(true, map.get("active"));

        HashMap<String, Object> valuesGroup = getHashMapGroupCursorValues();
        Cursor cursorGroup = mockCursorObjectForGroup(valuesGroup);
        HashMap<String, Object> mapGroup = EntityBuilder.bindCursorToHashMap(Group.class, cursorGroup);

        Assert.assertEquals(valuesGroup.get("id"), mapGroup.get("id"));
        Assert.assertEquals(valuesGroup.get("name"), mapGroup.get("name"));
        Assert.assertEquals(valuesGroup.get("chef"), mapGroup.get("chef"));
        Assert.assertEquals(valuesGroup.get("members"), mapGroup.get("members"));
    }

    @Test
    public void shouldBindCursorToEntity() throws Exception {
        String id = UUID.randomUUID().toString();
        String name = "John Doe";
        int age = 25;
        int active = 1;

        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name);
        values.put("age", age);
        values.put("active", active);
        Cursor cursor = mockCursorObjectForPerson(values);
        Person person = EntityBuilder.bindCursorToEntity(Person.class, cursor);

        Assert.assertEquals(values.get("id"), person.id);
        Assert.assertEquals(values.get("name"), person.name);
        Assert.assertEquals(values.get("age"), person.age);
        Assert.assertEquals(true, person.active);
    }

    private <T> T getDefaultInstance(Class<T> classType) throws Exception {
        Constructor<?> constructor = classType.getDeclaredConstructors()[0];
        Object[] parameters = EntityBuilder.getDefaultConstructorParameters(constructor.getParameterTypes());
        return (T)constructor.newInstance(parameters);
    }

    private HashMap<String, Object> getHashMapValues(boolean activeAsBoolean) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", UUID.randomUUID().toString());
        values.put("name", "John Doe");
        values.put("age", 25);
        if (activeAsBoolean) {
            values.put("active", true);
        } else {
            values.put("active", 1);
        }

        return values;
    }

    private HashMap<String, Object> getHashMapGroupValues() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", UUID.randomUUID().toString());
        values.put("name", "Super group");
        values.put("chef", new Person("qwer", "chef", 80, true));
        List<Person> members = new ArrayList<>();
        members.add(new Person("asdf", "member1", 20, true));
        members.add(new Person("wert", "member2", 21, true));
        members.add(new Person("sdfg", "member3", 22, false));
        values.put("members", members);
        return values;
    }

    private HashMap<String, Object> getHashMapGroupCursorValues() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", UUID.randomUUID().toString());
        values.put("name", "Super group");
        values.put("chef", "qwer");
        values.put("members", 1);
        return values;
    }

    private Cursor mockCursorObjectForPerson(final HashMap<String, Object> values) throws Exception {
        Cursor cursor = Mockito.mock(Cursor.class);
        Mockito.when(cursor.getColumnIndex("id")).thenReturn(0);
        Mockito.when(cursor.getColumnIndex("name")).thenReturn(1);
        Mockito.when(cursor.getColumnIndex("age")).thenReturn(2);
        Mockito.when(cursor.getColumnIndex("active")).thenReturn(3);
        Mockito.when(cursor.getString(0)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)values.get("id");
            }
        });
        Mockito.when(cursor.getString(1)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)values.get("name");
            }
        });
        Mockito.when(cursor.getInt(2)).thenReturn((int)values.get("age"));
        Mockito.when(cursor.getInt(3)).thenReturn((int)values.get("active"));
        return cursor;
    }

    private Cursor mockCursorObjectForGroup(final HashMap<String, Object> values) throws Exception {
        Cursor cursor = Mockito.mock(Cursor.class);
        Mockito.when(cursor.getColumnIndex("id")).thenReturn(0);
        Mockito.when(cursor.getColumnIndex("name")).thenReturn(1);
        Mockito.when(cursor.getColumnIndex("chef")).thenReturn(2);
        Mockito.when(cursor.getColumnIndex("member")).thenReturn(3);
        Mockito.when(cursor.getString(0)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)values.get("id");
            }
        });
        Mockito.when(cursor.getString(1)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)values.get("name");
            }
        });
        Mockito.when(cursor.getString(2)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)values.get("chef");
            }
        });
        Mockito.when(cursor.getInt(3)).thenReturn(1);
        return cursor;
    }

}
