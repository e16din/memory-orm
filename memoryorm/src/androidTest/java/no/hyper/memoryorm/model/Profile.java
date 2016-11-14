package no.hyper.memoryorm.model;

/**
 * Created by jean on 11.11.2016.
 */

public class Profile {

    private String id;
    private String name;
    private Integer age;
    private Boolean human;

    public Profile(String id, String name, Integer age, Boolean human) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.human = human;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getHuman() {
        return human;
    }

    public void setHuman(Boolean human) {
        this.human = human;
    }

}