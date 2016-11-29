package no.hyper.memoryorm.model;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by jean on 16.06.2016.
 */

@Keep
public class Database {

    private List<Table> tables;

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

}
