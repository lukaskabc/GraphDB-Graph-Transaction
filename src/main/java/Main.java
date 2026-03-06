import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

public class Main {
    // expects "test" GraphDB repository running on localhost:7200
    // Parameters:
    //      RDFS-Plus (Optimized)
    //      disable owl:sameAs
    //      32-bit
    //      Enable context index
    //      Enable predicate list index
    static final String REPOSITORY = "test";
    static final String GRAPH_DB_URL = "http://localhost:7200/";

    static final String GRAPH_1 = "http://example.com/graph";
    static final String GRAPH_2 = "http://example.com/graph2";
    public static void main(String[] args) {
        RepositoryManager manager = new RemoteRepositoryManager(GRAPH_DB_URL);
        manager.init();

        Repository repo = manager.getRepository(REPOSITORY);

        try (RepositoryConnection conn = repo.getConnection()) {

            // Execute the individual steps sequentially
            createGraph(conn);
            assertGraph1Exists(conn);
            moveGraphAndRollback(conn);
            checkGraphs(conn);

        } finally {
            manager.shutDown();
        }
    }

    private static void createGraph(RepositoryConnection conn) {
        System.out.println("Creating graph");
        conn.begin();
        try {
            // Delete graphs if they exist
            ValueFactory factory = conn.getValueFactory();
            conn.clear(factory.createIRI(GRAPH_1), factory.createIRI(GRAPH_2));

            // Insert data
            String insertQuery = "INSERT DATA { " +
                    "  GRAPH <" + GRAPH_1 + "> { " +
                    "    <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> . " +
                    "  } " +
                    "}";
            conn.prepareUpdate(QueryLanguage.SPARQL, insertQuery).execute();

            conn.commit();
            System.out.println("Transaction committed.");
        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException("Failed to create graph 1", e);
        }
    }

    private static void assertGraph1Exists(RepositoryConnection conn) {
        conn.begin();
        try {
            String askQuery = "ASK WHERE { " +
                    "  GRAPH <" + GRAPH_1 + "> { " +
                    "    <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> . " +
                    "  } " +
                    "}";
            boolean exists = conn.prepareBooleanQuery(QueryLanguage.SPARQL, askQuery).evaluate();

            if (!exists) {
                throw new RuntimeException("Assertion failed: Graph 1 does not exist or is missing the expected triple.");
            }

            System.out.println("Assertion passed: Data found in Graph 1.");
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    private static void moveGraphAndRollback(RepositoryConnection conn) {
        System.out.println("Moving graph");
        conn.begin();
        try {
            String moveQuery = "MOVE GRAPH <" + GRAPH_1 + "> TO <" + GRAPH_2 + ">";
            conn.prepareUpdate(QueryLanguage.SPARQL, moveQuery).execute();

            System.out.println("Graph moved, rollbacking transaction");
            conn.rollback();

            System.out.println("Transaction rolled back.");
        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException("Failed during moveAndRollbackTransaction", e);
        }
    }

    private static void checkGraphs(RepositoryConnection conn) {
        System.out.println("Checking graphs");

        String askGraph1 = "ASK WHERE { GRAPH <" + GRAPH_1 + "> { ?s ?p ?o } }";
        String askGraph2 = "ASK WHERE { GRAPH <" + GRAPH_2 + "> { ?s ?p ?o } }";

        boolean graph1Exists = conn.prepareBooleanQuery(QueryLanguage.SPARQL, askGraph1).evaluate();
        boolean graph2Exists = conn.prepareBooleanQuery(QueryLanguage.SPARQL, askGraph2).evaluate();

        System.out.println("Does Graph 1 exist? " + graph1Exists + " (Expected: true)");
        System.out.println("Does Graph 2 exist? " + graph2Exists + " (Expected: false)");

        if (!graph1Exists || graph2Exists) {
            throw new RuntimeException("Graph check failed: Graph 1 should exist and Graph 2 should not exist.");
        }
    }
}
