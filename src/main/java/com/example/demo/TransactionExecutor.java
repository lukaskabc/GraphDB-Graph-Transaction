package com.example.demo;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.transactions.EntityTransaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class TransactionExecutor {
    private final EntityManager entityManager;

    public TransactionExecutor(@Qualifier("jopaEntityManager") EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void assertNoTransaction() {
        if (entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("Transaction is active!");
        }
    }


    @Transactional
    public void createGraph() {
        System.out.println("Creating graph...");
        entityManager.createNativeQuery("""
                INSERT DATA {
                  GRAPH <http://example.com/graph> {
                    <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .
                  }
                }
                """).executeUpdate();
    }

    @Transactional
    public void assertGraphExists() {
        System.out.println("Asserting graph1 exists...");
        final boolean exist =
                entityManager.createNativeQuery("""
                        ASK WHERE {
                            GRAPH <http://example.com/graph> {
                                <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .
                            }
                        }
                        """, Boolean.class).getSingleResult();
        if (!exist) {
            throw new IllegalStateException("Graph does not exist!");
        }
    }

    @Transactional
    public void assertDefaultGraphDataNotExists() {
        System.out.println("Asserting data in default graph does not exists...");
        final boolean exist =
                entityManager.createNativeQuery("""
                        ASK WHERE {
                            <http://example.com/defaultSubject> <http://example.com/defaultPredicate> <http://example.com/defaultObject> .
                        }
                        """, Boolean.class).getSingleResult();
        if (exist) {
            throw new IllegalStateException("Data in default graph does exist!");
        }
        System.out.println("Data in default graph does not exist, correctly rollbacked");
    }

    private void insertDataToDefaultGraph() {
        System.out.println("Inserting data to default graph...");
        entityManager.createNativeQuery("""
                INSERT DATA {
                    <http://example.com/defaultSubject> <http://example.com/defaultPredicate> <http://example.com/defaultObject> .
                }
                """).executeUpdate();
    }

    private void insertDataToDefaultMoveGraph() {
        insertDataToDefaultGraph();
        System.out.println("Moving graph..");
        entityManager.createNativeQuery("""
                MOVE GRAPH <http://example.com/graph> TO <http://example.com/graph2>
                """).executeUpdate();
    }

    public void insertDataToDefaultMoveGraphAndRollback() {
        var transaction = entityManager.getTransaction();
        transaction.begin();

        assertTransactionActive();
        insertDataToDefaultMoveGraph();
        assertTransactionActive();
        System.out.println("Now rollbacking transaction...");
        transaction.rollback();
    }

    private void assertTransactionActive() {
        final boolean isActive = entityManager.getTransaction().isActive();
        if (!isActive) {
            throw new IllegalStateException("Transaction is not active!");
        }
    }

    @Transactional
    public void insertDataToDefaultMoveGraphAndThrow() {
        assertTransactionActive();
        insertDataToDefaultMoveGraph();
        assertTransactionActive();
        System.out.println("Now throwing exception to trigger rollback...");
        throw new IntentionalRuntimeException("Intentional exception to test transaction rollback");
    }

    @Transactional
    public void assertGraph2DoesNotExist() {
        System.out.println("Asserting graph2 does not exist...");
        final boolean exist =
                entityManager.createNativeQuery("""
                        ASK WHERE {
                            GRAPH <http://example.com/graph2> {
                                <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .
                            }
                        }
                        """, Boolean.class).getSingleResult();
        if (exist) {
            throw new IllegalStateException("Graph2 still exists!");
        }
    }
}
