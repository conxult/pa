/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.conxult.pa.annotation.handler;

import de.conxult.annotation.processor.ClassHandler;
import de.conxult.annotation.processor.ConxultAnnotationHandler;
import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author joerg
 */
public class PaAnnotationHandler
    extends ConxultAnnotationHandler {

    @ClassHandler
    public void handleHistoryTable(PaHistoryTable paHistoryTable, TypeElement entityType) {
        String entityClassName = entityType.getQualifiedName().toString();
        String simpleClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1);
        String historyClassName = simpleClassName + "Hist";

        log.info("handle {0} on {1}", paHistoryTable, entityClassName);
        log.info("  generate {0} ", historyClassName);

        var historyBuilder = new HistoryBuilder(paHistoryTable, entityType);

        boolean hasParentClass = historyBuilder.getParentClassName() != null;

        log.info("  columns: {0}", String.join(",", historyBuilder.columns.keySet()));

        var historyClass = createClass(historyClassName);
        historyClass.setPackage(entityClassName.substring(0, entityClassName.lastIndexOf('.')));
        historyClass.addImport(OffsetDateTime.class);
        historyClass.addImport(UUID.class);

        historyClass.addAnnotation(Generated.class)
            .setStringValue("value", getClass().getName())
            .setStringValue("date", OffsetDateTime.now().toString())
            .setStringValue("comments", "from: " + entityClassName);

        if (entityType.getAnnotation(MappedSuperclass.class) != null) {
            historyClass.addAnnotation(MappedSuperclass.class);
        } else if (entityType.getAnnotation(Entity.class) != null) {
            historyClass.addAnnotation(Entity.class);
        }

        var table = entityType.getAnnotation(Table.class);
        if (table != null) {
            historyClass.addAnnotation(Table.class)
                .setStringValue("catalog", table.catalog())
                .setStringValue("schema", table.schema())
                .setStringValue("name", table.name() + "_hist")
            ;
        }

        if (hasParentClass) {
            historyClass.setSuperType(historyBuilder.getParentClassName() + "Hist");
        } else {
            var historizedField = historyClass.addField()
              .setName("historizedAt")
              .setType(OffsetDateTime.class);
            historizedField
              .addAnnotation(Id.class);
            historizedField
              .addAnnotation(Column.class)
              .    setStringValue("name", "historized_at");
            historyClass.addField()
              .setName("historizedBy")
              .setType(UUID.class)
              .addAnnotation(Column.class)
              .    setStringValue("name", "historized_by");
            historyClass.addField()
              .setName("historizedComment")
              .setType(String.class)
              .addAnnotation(Column.class)
              .    setStringValue("name", "historized_comment");
        }

        historyBuilder.getColumns().values().forEach(c -> c.createField(historyClass.addField()));

       // default constructor
        historyClass.addMethod()
            .setPublic()
            .setConstructor(true)
            .setBody("");

        // constructor(other, historizedComment)
        var historyConstructor = historyClass.addMethod()
            .setPublic()
            .setConstructor(true);

        historyConstructor.addParameter(simpleClassName, "other");
        historyConstructor.addParameter(String.class, "historizedComment");

        historyConstructor.setBody("this(other, historizedComment, null);");

        // constructor(other, historizedComment, historizedBy)
        historyConstructor = historyClass.addMethod()
            .setPublic()
            .setConstructor(true);

        historyConstructor.addParameter(simpleClassName, "other");
        historyConstructor.addParameter(String.class, "historizedComment");
        historyConstructor.addParameter(UUID.class, "historizedBy");

        StringBuilder body = new StringBuilder();
        if (hasParentClass) {
            body.append("super(other, historizedComment, historizedBy);\n");
        } else {
            body.append("this.historizedComment = historizedComment;\n");
            body.append("this.historizedBy = historizedBy;\n");
        }
        historyBuilder.getColumns().keySet().forEach(name ->
            body.append("this." + name + " = other." + name + ";\n"));
        historyConstructor.setBody(body.toString());

        if (!hasParentClass) {
            var onPrePersistMethod = historyClass.addMethod()
                .setPublic()
                .setName("onPrePersist")
                .setBody("historizedAt = OffsetDateTime.now();");

            onPrePersistMethod.addAnnotation(PrePersist.class);
        }
    }


}
