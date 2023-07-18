package de.conxult.pa;

import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class JPQL {

  String          jpql;
  List<Parameter> parameters = new ArrayList<>();

  public static JPQL select(String jpql) {
    return new JPQL("select " + jpql);
  }

  public JPQL(String jpql) {
    this.jpql = jpql;
  }

  public JPQL addParameter(String name, Object value) {
    parameters.add(new Parameter(name, value));
    return this;
  }

  public JPQL addParameter(Object value) {
      parameters.add(new Parameter("?"+(parameters.size()+1), value));
      return this;
  }

  public JPQL addParameters(Object... values) {
    for (Object value : values) {
      addParameter(value);
    }
    return this;
  }

  public String getJpql() {
    return jpql.toString();
  }

  public <T> TypedQuery<T> setQueryParameters(TypedQuery<T> query) {
    for (Parameter parameter : parameters) {
      if (parameter.name.startsWith("?")) {
        query.setParameter(Integer.parseInt(parameter.name.substring(1)), parameter.value);
      } else {
        query.setParameter(parameter.name, parameter.value);
      }
    }
    return query;
  }

  public class Parameter {
    String name;
    Object value;

    Parameter(String name, Object value) {
      this.name = name;
      this.value = value;
    }

  }

  @Override
  public String toString() {
    return String.format("{sql:'%s',", jpql);
  }


}
