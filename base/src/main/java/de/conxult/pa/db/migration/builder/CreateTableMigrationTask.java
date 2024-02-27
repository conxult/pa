/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.db.migration.builder;

import de.conxult.pa.db.migration.CxPaBaseJavaMigration;
import de.conxult.util.ClassCache;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
public class CreateTableMigrationTask
    extends MigrationTask {

    Class<?> entityClass;
    Table    table;
    boolean  ifNotExists = false;

    public CreateTableMigrationTask(CxPaBaseJavaMigration owner, Class<?> tableClass) {
        super(owner);
        this.entityClass = tableClass;
        this.table = tableClass.getAnnotation(Table.class);
    }

    public CreateTableMigrationTask ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    public void runTask() {
        EntityManager em = getEntityManager(table.schema());

        Map<String, Field> columnFields = new LinkedHashMap<>();
        for (Field field : ClassCache.of(entityClass).getFields(Column.class)) {
            columnFields.put(field.getName(), field);
        }

        Map<String, Field>  embeddedFields  = new LinkedHashMap<>();
        Map<String, Column> embeddedColumns = new LinkedHashMap<>();
        for (Field field : ClassCache.of(entityClass).getFields(Embedded.class)) {
            var fieldName = field.getName();
            var fieldClass = field.getType();

            for (Field embeddedField : ClassCache.of(fieldClass).getFields(Column.class)) {
                embeddedFields.put(fieldName + "." + embeddedField.getName(), embeddedField);
                embeddedColumns.put(fieldName + "." + embeddedField.getName(), embeddedField.getAnnotation(Column.class));
            }
            for (AttributeOverride attributeOverride : field.getAnnotationsByType(AttributeOverride.class)) {
                embeddedColumns.put(fieldName + "." + attributeOverride.name(), attributeOverride.column());
            }
        }

        {
            StringBuilder createTableSql = new StringBuilder()
                .append("create table ");

            if (ifNotExists) {
                createTableSql.append("if not exists ");
            }

            createTableSql.append(table.schema() + "." + table.name());
            String delim = " (\n  ";
            for (Field columnField : columnFields.values()) {
                createTableSql.append(delim)
                    .append(getColumnSql(columnField.getType(), columnField.getAnnotation(Column.class)));
                delim = ",\n  ";
            }
            for (String embeddedFieldName : embeddedFields.keySet()) {
                createTableSql.append(delim)
                    .append(getColumnSql(
                        embeddedFields.get(embeddedFieldName).getType(),
                        embeddedColumns.get(embeddedFieldName)));
            }

            var createPkConstraintSql = createPKConstraintSql(table, columnFields, entityClass);
            if (!createPkConstraintSql.isEmpty()) {
                createTableSql.append(delim).append(createPkConstraintSql);
            }

            createTableSql.append(")");

            log.info("createTableSql: %s", createTableSql.toString());
            em.createNativeQuery(createTableSql.toString()).executeUpdate();
        }

//    CONSTRAINT permission_pk PRIMARY KEY (principal)


        for (UniqueConstraint uniqueConstraint : table.uniqueConstraints()) {
//            sql.append(delim).append("  ", )
        }

        for (Index index : table.indexes()) {
            StringBuilder createIndexSql = new StringBuilder()
                .append("create " + (index.unique() ? "unique " : "") + "index ")
                .append("if not exists ")
                .append(table.name() + "_" + index.name() + " ")
                .append("on " + table.schema() + "." + table.name() + " ")
                .append("(" + index.columnList() + ")");

            log.info("createIndexSql: %s", createIndexSql);
            em.createNativeQuery(createIndexSql.toString()).executeUpdate();
        }

    }

    String getColumnSql(Class<?> type, Column column) {
        return new StringBuilder()
            .append(column.name())
            .append(" ")
            .append(getColumnType(type))
            .append(column.nullable() ? "" : " not null")
            .toString();
    }

    String getColumnType(Class fieldClass) {
        if (fieldClass == String.class) {
            return "text";
        }
        if (fieldClass == UUID.class) {
            return "uuid";
        }
        if (fieldClass == OffsetDateTime.class) {
            return "timestamptz";
        }
        if (fieldClass == Long.class || fieldClass == long.class) {
            return "long";
        }
        if (fieldClass == Integer.class || fieldClass == int.class) {
            return "integer";
        }
        if (fieldClass == Boolean.class || fieldClass == boolean.class) {
            return "boolean";
        }
        if (fieldClass == Double.class || fieldClass == double.class) {
            return "double";
        }
        return "unknown";
    }

    StringBuilder createPKConstraintSql(Table table, Map<String, Field> columnFields, Class<?> entityClass) {
        StringBuilder pkConstraint = new StringBuilder();


        String delim = "constraint " + table.name() + "_pk primary key (";

        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            for (Field field : ClassCache.of(idClass.value()).getFields()) {
                pkConstraint.append(delim);
                pkConstraint.append(columnFields.get(field.getName()).getAnnotation(Column.class).name());
                delim = ", ";
            }

        } else {
            for (Field field : ClassCache.of(entityClass).getFields(Id.class)) {
                pkConstraint.append(delim);
                pkConstraint.append(field.getAnnotation(Column.class).name());
                delim = ", ";
            }

        }

        return (pkConstraint.isEmpty()) ? pkConstraint : pkConstraint.append(")");
    }

}
