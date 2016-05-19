package no.hyper.memoryorm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jean on 5/19/2016.
 */
public class QueryBuilder {

    private Operation operation;
    private List<String> columns = new ArrayList<>();
    private String from = "";
    private String condition = "";
    private String join = "";

    private enum Operation {
        SELECT
    }

    public QueryBuilder select() {
        operation = Operation.SELECT;
        return this;
    }

    public QueryBuilder fields() {
        columns.add("*");
        return this;
    }

    public QueryBuilder fields(List<String> fields) {
        this.columns.addAll(fields);
        return this;
    }

    public QueryBuilder from(String table) {
        from = "from " + table;
        return this;
    }

    public QueryBuilder where(String condition) {
        this.condition = "WHERE " + condition;
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
        if(join != "") {
            sb.append(join);
            sb.append(" ");
        }
        sb.append(condition);
        return sb.toString();
    }

}
