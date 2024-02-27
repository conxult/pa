/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.db.migration;

import de.conxult.pa.db.migration.builder.CreateTableMigrationTask;
import de.conxult.pa.db.migration.builder.MigrationTask;
import de.conxult.util.ClassUtil;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
public abstract class CxPaBaseJavaMigration
    extends BaseJavaMigration {

    Context context;

    Map<String, EntityManager> entityManagers = new HashMap<>();

    List<MigrationTask> tasks = new LinkedList<>();

    @Transactional
    public abstract void migrate();

    @Override
    public void migrate(Context context) throws Exception {
        var migrator = (CxPaBaseJavaMigration)CDI.current()
            .select(ClassUtil.normalize(getClass()))
            .get();
        migrator
            .setContext(context)
            .migrate();
        migrator.destroy();
    }

    @Override
    public Integer getChecksum() {
        return ClassUtil.normalize(getClass()).getName().hashCode();
    }

    protected CreateTableMigrationTask createTable(Class tableClass) {
        return new CreateTableMigrationTask(this, tableClass);
    }

    protected MigrationTask alterTable(Class tableClass) {
        return null;
    }

    public EntityManager getEntityManager(String schema) {
        return entityManagers.computeIfAbsent(schema, CxPaFactory::createEntityManager);
    }

    @Transactional
    public void runUnfinishedTasks() {
        for (MigrationTask task : tasks) {
            if (!task.isDone()) {
                task.runTask();
                task.setDone(true);
            }
        }
    }

    public void destroy() {
        runUnfinishedTasks();
        for (EntityManager em : entityManagers.values()) {
            CDI.current().destroy(em);
        }
        CDI.current().destroy(this);
    }
}

