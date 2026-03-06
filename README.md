# Jopa transaction problem

When a graph is moved or copied as a part of an transaction, the action is not rollbacked if the transaction is rolled back.

[`TestRunner`](src/main/java/com/example/demo/TestRunner.java) waits for the spring app to start and then runs the tests.

Using the RDF4J manually with manual transaction control works correctly with a new repository manager or repository from jopa after unwrapping the entity manager.
And the operation (move or copy) is rollbacked if the transaction is rolled back.

**With Jopa, the operation is not rollbacked properly.**

