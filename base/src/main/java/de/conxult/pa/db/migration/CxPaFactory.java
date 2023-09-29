/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.db.migration;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class CxPaFactory {

    static public EntityManager createEntityManager(String schema) {
        return CDI.current()
            .select(
                EntityManager.class,
                new PersistenceUnit.PersistenceUnitLiteral(schema))
            .get();
    }

//    String getSchema(InjectionPoint ip) {
//        if (ip.getQualifiers() != null) {
//            for (Annotation qualifier : ip.getQualifiers()) {
//                if (PaSchema.class.isAssignableFrom(qualifier.getClass())) {
//                    return ((PaSchema)qualifier).value();
//                }
//            }
//        }
//
//        if (ip.getAnnotated() != null) {
//            var paSchema = ip.getAnnotated().getAnnotation(PaSchema.class);
//            if (paSchema != null) {
//                return paSchema.value();
//            }
//        }
//
//        if (ip.getBean() != null) {
//            var paSchema = ip.getBean().getBeanClass().getAnnotation(PaSchema.class);
//            if (paSchema != null) {
//                return paSchema.value();
//            }
//        }
//
//        return "no-@pa-schema";
//    }


}
