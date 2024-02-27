/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

/**
 *
 * @author joerg
 */
@ApplicationScoped
@Unremovable
public class CxEntityManagerFactory {

    public EntityManager getEntityManager(String schema) {
        return CDI.current()
            .select(
                EntityManager.class,
                new PersistenceUnit.PersistenceUnitLiteral(schema))
            .get();
    }

}


