package com.example.demo;

import com.github.ledsoft.jopa.spring.transaction.DelegatingEntityManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.function.Consumer;

import static com.example.demo.DemoApplication.GRAPH_DB_URL;
import static com.example.demo.DemoApplication.REPOSITORY;

@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class TestRunner implements SmartInitializingSingleton {

    final TransactionExecutor executor;
    public TestRunner(TransactionExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("Persisting new entity");
        executor.createEntity(TransactionExecutor.id);

        executor.inTransaction(() ->{
            // var entity = executor.findEntity(); // em.find works
            var entity = executor.findEntityNative(); // native query does not work
            executor.updateDateMerge(entity);
            // detaching the entity first and merging explicitly works
            // executor.updateDateDetached(entity);
        });

        final URI graph = executor.findDateGraph();
        if (graph.equals(ClassInContext.u_CLASS_IRI)) {
            System.out.println("Tested passed");
        } else {
            System.out.println("Date property is in graph: " + graph);
            throw new RuntimeException("Test failed");
        }

        if (!executor.getDate().equals(TransactionExecutor.newDate)) {
            throw new RuntimeException("date not updated");
        }

    }
}
