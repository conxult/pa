/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.conxult.pa.annotation.handler;

import de.conxult.annotation.processor.ClassHandler;
import de.conxult.annotation.processor.ConxultAnnotationHandler;
import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
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

        var table = entityType.getAnnotation(Table.class);
        if (table != null) {
            historyClass.addAnnotation(Table.class)
                .setStringValue("catalog", table.catalog())
                .setStringValue("schema", table.schema())
                .setStringValue("name", table.name() + "_hist")
            ;
        }

        if (hasParentClass) {
            historyClass.addAnnotation(MappedSuperclass.class);
            historyClass.setSuperType(historyBuilder.getParentClassName() + "Hist");
        } else {
            historyClass.addField()
              .setName("historizedAt")
              .setType(OffsetDateTime.class)
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

        historyBuilder.getColumns().values().forEach(c -> {
            var column = c.column;
            var field = historyClass.addField()
                .setType(c.className)
                .setName(c.name)
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
        });

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
