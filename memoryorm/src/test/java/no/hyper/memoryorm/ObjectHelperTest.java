package no.hyper.memoryorm;

import android.content.Context;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by jean on 01.06.2016.
 */
public class ObjectHelperTest {

    private class Person {

        private String id;
        private String name;
        private int age;
        private boolean active;

    }

    private class Group {

        private String id;
        private String name;
        private Person chef;
        private List<Person> members;
        private List<String> departments;

    }

    @Test
    public void shouldGetDeclaredFields() {
        assertNumberOfFields(4, ObjectHelper.getDeclaredFields(Person.class));
        assertNumberOfFields(5, ObjectHelper.getDeclaredFields(Group.class));
    }

    @Test
    public void shouldSayIfCustomType() {
        List<Field> fields = ObjectHelper.getDeclaredFields(Person.class);
        for (Field field : fields) {
            Assert.assertFalse(ObjectHelper.isCustomType(field.getType().getSimpleName()));
        }

        List<Field> fieldsGroup = ObjectHelper.getDeclaredFields(Group.class);
        for (Field field : fieldsGroup) {
            if (field.getName().equals("chef") || field.getName().equals("members")
                    || field.getName().equals("departments")) {
                Assert.assertTrue(ObjectHelper.isCustomType(field.getType().getSimpleName()));
            } else {
                Assert.assertFalse(ObjectHelper.isCustomType(field.getType().getSimpleName()));
            }
        }
    }

    @Test
    public void shouldGetEquivalentSqlType() {
        List<Field> fields = ObjectHelper.getDeclaredFields(Person.class);
        for (Field field : fields) {
            switch (field.getName()) {
                case "id":
                case "name": Assert.assertEquals(ObjectHelper.getEquivalentSqlType(field.getType()), "TEXT"); break;
                case "age":
                case "active": Assert.assertEquals(ObjectHelper.getEquivalentSqlType(field.getType()), "INTEGER"); break;
            }
        }

        List<Field> fieldsGroup = ObjectHelper.getDeclaredFields(Group.class);
        for (Field field : fieldsGroup) {
            switch (field.getName()) {
                case "id":
                case "name": Assert.assertEquals(ObjectHelper.getEquivalentSqlType(field.getType()), "TEXT"); break;
                case "chef":
                case "members": Assert.assertEquals(ObjectHelper.getEquivalentSqlType(field.getType()), "INTEGER"); break;
            }
        }
    }

    @Test
    public void shouldGetEquivalentSqlContent() {
        List<Field> fields = ObjectHelper.getDeclaredFields(Person.class);
        String content = ObjectHelper.getEquivalentSqlContent(fields, null);
        Assert.assertTrue(content.contains("id TEXT PRIMARY KEY"));
        Assert.assertTrue(content.contains("name TEXT"));
        Assert.assertTrue(content.contains("age INTEGER"));
        Assert.assertTrue(content.contains("active INTEGER"));

        List<Field> fieldsGroup = ObjectHelper.getDeclaredFields(Group.class);
        String contentGroup = ObjectHelper.getEquivalentSqlContent(fieldsGroup, null);
        Assert.assertTrue(contentGroup.contains("id TEXT PRIMARY KEY"));
        Assert.assertTrue(contentGroup.contains("name TEXT"));
        Assert.assertTrue(contentGroup.contains("chef INTEGER"));
        Assert.assertTrue(contentGroup.contains("members INTEGER"));
    }

    @Test
    public void shouldGetActualListType() {
        for(Field field : ObjectHelper.getDeclaredFields(Group.class)) {
            if (ObjectHelper.isAList(field)) {
                Assert.assertTrue(ObjectHelper.getActualListType(field).getSimpleName().equals(Person.class.getSimpleName())
                        || ObjectHelper.getActualListType(field).getSimpleName().equals(String.class.getSimpleName()));
            }
        }
    }

    private void assertNumberOfFields(int number, List<Field> fields) {
        Assert.assertEquals(number, fields.size());
    }

}
