package com.example.demo;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.rdf4j.Rdf4jDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

import static com.example.demo.DemoApplication.REPOSITORY_URL;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.*;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PersistenceFactoryConfig {

    private EntityManagerFactory factory;

    @PreDestroy
    private void close() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    @Bean
    @Primary
    public @Nullable EntityManagerFactory entityManagerFactory() {
        return factory;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = new HashMap<>();

        properties.put(JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());

        properties.put(ONTOLOGY_PHYSICAL_URI_KEY, REPOSITORY_URL);
        properties.put(DATA_SOURCE_CLASS, Rdf4jDataSource.class.getName());

        this.factory = Persistence.createEntityManagerFactory("examplePersistenceUnit", properties);
    }
}
