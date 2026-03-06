package com.example.demo;

import org.springframework.stereotype.Component;

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

}
