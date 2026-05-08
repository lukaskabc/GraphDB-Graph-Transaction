package com.example.demo;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.transactions.EntityTransaction;
import org.eclipse.rdf4j.query.algebra.In;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionExecutor {
    private final EntityManager entityManager;

    public TransactionExecutor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static final URI id = URI.create("http://example/entity/instance");
    public static final URI id2 = URI.create("http://example/entity/instance2");
    public static final Instant initialDate = Instant.EPOCH;
    public static final Instant newDate = Instant.now();

    @Transactional
    public void createEntity(URI identifier) {
        System.out.println("Creating graph...");
        final ClassInContext entity = new ClassInContext();
        entity.setId(identifier);
        entity.setDate(initialDate);
        entityManager.persist(entity);
    }

    public void updateDateMerge(ClassInContext instance) {
        instance.setDate(newDate);
        // merged automatically
        // explicit merge makes no difference on test result
        // entityManager.merge(instance);
    }

    // this saves update to correct context
    public void updateDateDetached(ClassInContext instance) {
        entityManager.detach(instance);
        instance.setDate(newDate);
        entityManager.merge(instance);
    }

    public ClassInContext findEntityNative() {
        return entityManager.createNativeQuery("""
                SELECT ?entity FROM ?graph WHERE {
                    ?entity a ?type .
                }
                """, ClassInContext.class)
                .setParameter("graph", ClassInContext.u_CLASS_IRI)
                .setParameter("type", ClassInContext.u_CLASS_IRI)
                .getSingleResult();
    }

    public ClassInContext findEntity() {
        return entityManager.find(ClassInContext.class, id);
    }

    @Transactional(readOnly = true)
    public URI findDateGraph() {
        return entityManager.createNativeQuery("""
                SELECT ?graph WHERE {
                    GRAPH ?graph {
                        ?entity ?hasDate ?date .
                    }
                }
                """, URI.class)
                .setParameter("entity", id)
                .setParameter("hasDate", ClassInContext.u_DATE_IRI)
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    public Instant getDate() {
        return entityManager.find(ClassInContext.class, id).getDate();
    }

    @Transactional
    public void inTransaction(Runnable runnable) {
        runnable.run();
    }
}
