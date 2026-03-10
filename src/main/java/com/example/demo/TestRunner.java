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

import java.util.function.Consumer;

import static com.example.demo.DemoApplication.GRAPH_DB_URL;
import static com.example.demo.DemoApplication.REPOSITORY;

@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class TestRunner implements SmartInitializingSingleton {
    private final Jopa_Test jopa;
    private final RDF4J_Test rdf4j;
    private final DelegatingEntityManager entityManager;

    public TestRunner(Jopa_Test jopa, RDF4J_Test rdf4j, DelegatingEntityManager entityManager) {
        this.jopa = jopa;
        this.rdf4j = rdf4j;
        this.entityManager = entityManager;
    }

    private void reset() {
        System.out.println("### Removing existing graphs...");
        rdf4j.deleteGraphs();
    }

    @Override
    public void afterSingletonsInstantiated() {
        reset(); // Drop existing graphs in repository

//        System.out.println("### Running RDF4J test with a new RDF4J connection...");
//        withNewRDF4JConnection(rdf4j::execute);
//
//        reset();
//        System.out.println("### Running RDF4J test with a Jopa connection...");
//        rdf4j.withJopaTransaction(() -> withJopaConnection(rdf4j::execute));
//        System.out.println("RDF4J test completed successfully.");

        reset();
        System.out.println("### Running JOPA test...");
//        jopa.testWithManualTransaction();
//        reset();
        jopa.testWithSpringTransaction();
        System.out.println("JOPA test completed successfully.");
    }

    void withJopaConnection(Consumer<RepositoryConnection> action) {
        Repository repo = entityManager.unwrap(Repository.class);
        try (RepositoryConnection conn = repo.getConnection()) {
            action.accept(conn);
        }
    }

    void withNewRDF4JConnection(Consumer<RepositoryConnection> action) {
        RepositoryManager manager = new RemoteRepositoryManager(GRAPH_DB_URL);
        manager.init();

        Repository repo = manager.getRepository(REPOSITORY);

        try (RepositoryConnection conn = repo.getConnection()) {
            action.accept(conn);
        } finally {
            manager.shutDown();
        }
    }
}
