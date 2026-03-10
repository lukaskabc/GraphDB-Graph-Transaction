package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class Jopa_Test {
    private final TransactionExecutor transactionExecutor;

    public Jopa_Test( TransactionExecutor transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }

    public void testWithManualTransaction() {
        System.out.println("### Testing with manual transaction control...");
        transactionExecutor.assertNoTransaction();
        transactionExecutor.createGraph();
        transactionExecutor.assertGraphExists();

        transactionExecutor.insertDataToDefaultMoveGraphAndRollback();
        transactionExecutor.assertDefaultGraphDataNotExists();
        transactionExecutor.assertGraph2DoesNotExist();
        transactionExecutor.assertGraphExists();
    }

    public void testWithSpringTransaction() {
        System.out.println("### Testing with Spring transaction control...");
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
