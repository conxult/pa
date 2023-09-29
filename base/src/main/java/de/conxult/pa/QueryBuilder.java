/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa;

import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author joerg
 */
public class QueryBuilder {

    List<String>        whereClauses = new ArrayList<>();
    Map<String, Object> parameters   = new HashMap<>();

    public QueryBuilder addCondition(Object value, String whereClause, String parameterName) {
        if (value != null) {
            addWhereClause(whereClause);
            addParameter(parameterName, value);
        }

        return this;
    }

    public QueryBuilder addLike(String value, String columnName, String parameterName) {
        if (value != null && !value.isEmpty()) {
            addWhereClause(String.format("lower(%s) like :%s", columnName, parameterName));
            addParameter(parameterName, value.toLowerCase().replace('*', '%'));
        }

        return this;
    }

    public QueryBuilder addWhereClause(String whereClause) {
        whereClauses.add(whereClause);
        return this;
    }

    public QueryBuilder addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    public String getWhere() {
        return (whereClauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", whereClauses));
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Query query) {
        parameters.forEach((n, v) -> query.setParameter(n, v));
    }
}
