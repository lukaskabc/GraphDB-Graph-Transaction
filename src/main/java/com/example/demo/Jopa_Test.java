package com.example.demo;

import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Jopa_Test {

    private final TransactionExecutor transactionExecutor;

    public Jopa_Test(TransactionExecutor transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }

    public void execute() {
        // ensure that each call is standalone transaction
        transactionExecutor.assertNoTransaction();
        transactionExecutor.createGraph();
        transactionExecutor.assertGraphExists();
        try {
            transactionExecutor.moveGraphAndThrow();
        } catch (IntentionalRuntimeException e) {
            // as expected
        }
        // THE PROBLEM: this throws, the "graph" does not exist and "graph2" exists
        // the MOVE operation is not rollbacked!
        // and the same applies to graph copy
        transactionExecutor.assertGraph2DoesNotExist();
        transactionExecutor.assertGraphExists();
    }

    public static class IntentionalRuntimeException extends RuntimeException {
        public IntentionalRuntimeException(String message) {
            super(message);
        }
    }

    @Component
    public static class TransactionExecutor {
        private final EntityManager entityManager;

        public TransactionExecutor(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Transactional
        public void assertGraph2DoesNotExist() {
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

        @Transactional
        public void assertGraphExists() {
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

        public void assertNoTransaction() {
            if (entityManager.getTransaction().isActive()) {
                throw new IllegalStateException("Transaction is active!");
            }
        }

        @Transactional
        public void createGraph() {
            entityManager.createNativeQuery("""
                    INSERT DATA {
                      GRAPH <http://example.com/graph> {
                        <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .
                      }
                    }
                    """).executeUpdate();
        }

        @Transactional
        public void moveGraphAndThrow() {
            entityManager.createNativeQuery("""
                    COPY GRAPH <http://example.com/graph> TO <http://example.com/graph2>
                    """).executeUpdate();
            // @Transactional should rollback on RuntimeException
            throw new IntentionalRuntimeException("Intentional exception to test transaction rollback");
        }
    }
}
