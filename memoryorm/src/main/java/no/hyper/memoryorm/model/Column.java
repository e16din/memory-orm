package no.hyper.memoryorm.model;

/**
 * Created by jean on 16.06.2016.
 */
public class Column {

    private String label;
    private String type;
    private boolean primary = false;
    private boolean list = false;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }
}
