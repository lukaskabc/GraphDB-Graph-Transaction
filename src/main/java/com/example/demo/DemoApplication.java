package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({PersistenceFactoryConfig.class, PersistenceConfig.class})
@SpringBootApplication
public class DemoApplication {

    // expects "test" GraphDB repository running on localhost:7200
    // Parameters:
    //      RDFS-Plus (Optimized)
    //      disable owl:sameAs
    //      32-bit
    //      Enable context index
    //      Enable predicate list index
    static final String REPOSITORY = "test";
    static final String GRAPH_DB_URL = "http://localhost:7200/";
    static final String REPOSITORY_URL = GRAPH_DB_URL + "repositories/" + REPOSITORY;

    static final String GRAPH_1 = "http://example.com/graph";
    static final String GRAPH_2 = "http://example.com/graph2";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
