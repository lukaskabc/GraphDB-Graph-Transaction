package com.example.demo;

import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Jopa_Test {

    private final EntityManager springTransactionEntityManager;
    private final EntityManager jopaEntityManager;

    public Jopa_Test(@Qualifier("entityManager") EntityManager springTransactionEntityManager, @Qualifier("jopaEntityManager") EntityManager jopaEntityManager) {
        this.springTransactionEntityManager = springTransactionEntityManager;
        this.jopaEntityManager = jopaEntityManager;
    }

    public void testWithJopaManager() {
        System.out.println("### Testing with manual transaction control...");
        final TransactionExecutor transactionExecutor = new TransactionExecutor(jopaEntityManager);
        transactionExecutor.assertNoTransaction();
        transactionExecutor.createGraph();
        transactionExecutor.assertGraphExists();

        transactionExecutor.insertDataToDefaultMoveGraphAndRollback();
        transactionExecutor.assertDefaultGraphDataNotExists();
        transactionExecutor.assertGraph2DoesNotExist();
        transactionExecutor.assertGraphExists();
    }

    public void testWithDefaultManager() {
        System.out.println("### Testing with Spring transaction control...");
        final TransactionExecutor transactionExecutor = new TransactionExecutor(springTransactionEntityManager);
        transactionExecutor.assertNoTransaction();
        transactionExecutor.createGraph();
        transactionExecutor.assertGraphExists();
        try {
            transactionExecutor.insertDataToDefaultMoveGraphAndThrow();
        } catch (IntentionalRuntimeException e) {
            // as expected
        }
        transactionExecutor.assertDefaultGraphDataNotExists();
        transactionExecutor.assertGraph2DoesNotExist();
        transactionExecutor.assertGraphExists();
    }

}
