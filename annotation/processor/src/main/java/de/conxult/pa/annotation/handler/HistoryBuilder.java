/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.annotation.handler;

import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Getter;

/**
 *
 * @author joerg
 */

@Getter
class HistoryBuilder {

    String                   parentClassName;
    Map<String, EntityField> columns = new HashMap<>();

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
                Column column = variable.getAnnotation(Column.class);
                if (column != null) {
                    String variableName = variable.getSimpleName().toString();
                    columns.put(variableName, new EntityField(column, variable.asType().toString(), variableName));
                }
            }
        }
    }

    static class EntityField {
        Column column;
        String name;
        String className;

        public EntityField(Column column, String className, String name) {
            this.column = column;
            this.name = name;
            this.className = className;
        }


    }
//       String parameterType = parameter.asType().toString();
//        String parameterName = parameter.getSimpleName().toString();

}
