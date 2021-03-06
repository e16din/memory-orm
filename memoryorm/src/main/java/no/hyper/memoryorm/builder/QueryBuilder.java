package no.hyper.memoryorm.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean on 5/19/2016.
 */
public class QueryBuilder {

    private Operation operation;
    private List<String> columns = new ArrayList<>();
    private String from = "";
    private String where = "";
    private String join = "";

    private enum Operation {
        SELECT, DELETE
    }

    public QueryBuilder select() {
        operation = Operation.SELECT;
        columns.add("ROWID, *");
        return this;
    }

    public QueryBuilder select(String... fields) {
        operation = Operation.SELECT;
        for (String field : fields) {
            columns.add(field);
        }
        return this;
    }

    public QueryBuilder delete() {
        operation = Operation.DELETE;
        return this;
    }

    public QueryBuilder from(String table) {
        from = "from " + table;
        return this;
    }

    public QueryBuilder where(String condition) {
        if (condition != null) {
            this.where = "WHERE " + condition;
        }
        return this;
    }

    public QueryBuilder whereId(String id) {
        if (id != null) {
            this.where = "WHERE id='" + id + "'";
        }
        return this;
    }

    public QueryBuilder innerJoinById(String rightTable, String leftColumn, String rightColumn) {
        join += " INNER JOIN " + rightTable +
                " ON " + leftColumn + " = " + rightColumn;
        return this;
    }

    public String toSqlRequest() {
        StringBuilder sb = new StringBuilder();
        sb.append(operation.name());
        sb.append(" ");
        if(columns.size() > 0) {
            for(String column : columns) {
                sb.append(column);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ");
        }
        sb.append(" ");
        sb.append(from);
        sb.append(" ");
        if(!join.equals("")) {
            sb.append(join);
            sb.append(" ");
        }
        if(!where.equals("")) {
            sb.append(where);
        }
        sb.append(";");
        return sb.toString();
    }

}
