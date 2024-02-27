/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.db.migration.builder;

import de.conxult.log.Log;
import de.conxult.pa.db.migration.CxPaBaseJavaMigration;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Getter @Setter @Accessors(chain = true)
abstract public class MigrationTask {

    protected Log log = Log.instance(MigrationTask.class);
    
    CxPaBaseJavaMigration owner;
    boolean               done = false;

    protected MigrationTask(CxPaBaseJavaMigration owner) {
        this.owner = owner;
        owner.runUnfinishedTasks();
        owner.getTasks().add(this);
    }

    protected EntityManager getEntityManager(String schema) {
        return owner.getEntityManager(schema);
    }

    abstract public void runTask();

}
