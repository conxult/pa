/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.annotation.handler;

import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Getter;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 *
 * @author joerg
 */

@Getter
class HistoryBuilder {

    String                        parentClassName;
    Map<String, ColumnDefinition> columns = new HashMap<>();

    HistoryBuilder(PaHistoryTable paHistoryTable, TypeElement entityType) {
        parentClassName = entityType.getSuperclass().toString();
        var genericIndex = parentClassName.indexOf("<");
        if (genericIndex != -1) {
            parentClassName = parentClassName.substring(0, genericIndex);
        }
        if (parentClassName.equals(Object.class.getName())) {
            parentClassName = null;
        }

        collectFields((TypeElement)entityType);
    }

    void collectFields(TypeElement element) {
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement instanceof VariableElement variable) {
                Column   column   = variable.getAnnotation(Column.class);
                Id       id       = variable.getAnnotation(Id.class);
                Embedded embedded = variable.getAnnotation(Embedded.class);
                if (column != null || embedded != null) {
                    String variableName = variable.getSimpleName().toString();
                    columns.put(variableName, new ColumnDefinition(column, id, embedded, variable.asType().toString(), variableName));
                }
            }
        }
    }

    static class ColumnDefinition {
        Column   column;
        Id       id;
        Embedded embedded;
        String   name;
        String   className;

        public ColumnDefinition(Column column, Id id, Embedded embedded, String className, String name) {
            this.column = column;
            this.id = id;
            this.embedded = embedded;
            this.name = name;
            this.className = className;
        }

        void createField(FieldSource<JavaClassSource> field) {
            field
                .setType(className)
                .setName(name);

            if (id != null) {
                field.addAnnotation(Id.class);
            } else if (embedded != null) {
                field.addAnnotation(Embedded.class);
            }

            if (column != null) {
                field
                    .addAnnotation(Column.class)
                    .    setStringValue ("name"            , column.name())
                    .    setStringValue ("table"           , column.table())
                    .    setStringValue ("columnDefinition", column.columnDefinition())
                    .    setLiteralValue("unique"          , Boolean.toString(column.unique()))
                    .    setLiteralValue("nullable"        , Boolean.toString(column.nullable()))
                    .    setLiteralValue("insertable"      , Boolean.toString(column.insertable()))
                    .    setLiteralValue("updatable"       , Boolean.toString(column.updatable()))
                    .    setLiteralValue("length"          , Integer.toString(column.length()))
                    .    setLiteralValue("precision"       , Integer.toString(column.precision()))
                    .    setLiteralValue("scale"           , Integer.toString(column.scale()));
            }
        }

    }
//       String parameterType = parameter.asType().toString();
//        String parameterName = parameter.getSimpleName().toString();

}
