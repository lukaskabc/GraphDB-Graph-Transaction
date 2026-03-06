package com.example.demo;

import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionExecutor {
    private final EntityManager entityManager;

    public TransactionExecutor(EntityManager entityManager) {
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
    public void moveGraphAndThrow() {
        System.out.println("Moving graph..");
        entityManager.createNativeQuery("""
                MOVE GRAPH <http://example.com/graph> TO <http://example.com/graph2>
                """).executeUpdate();
        System.out.println("Graph moved, now throwing exception to trigger rollback...");
        // @Transactional should rollback on RuntimeException
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
