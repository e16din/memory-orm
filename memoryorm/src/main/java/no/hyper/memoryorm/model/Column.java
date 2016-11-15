package no.hyper.memoryorm.model;


import com.google.gson.annotations.SerializedName;

/**
 * Created by jean on 16.06.2016.
 */
public class Column {

    private String label;
    private String type;
    private boolean primary = false;
    private boolean list = false;
    private boolean custom = false;

    @SerializedName("enum")
    private boolean _enum = false;

    @SerializedName("foreign_key")
    private boolean foreignKey = false;

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isList() {
        return list;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isCustom() {
        return custom;
    }

    public boolean isEnum() {
        return _enum;
    }

}
