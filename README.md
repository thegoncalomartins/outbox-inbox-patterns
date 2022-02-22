# Building a Knowledge Base Service With Neo4j, Kafka, and the Outbox Pattern

This repository contains the code for a small project that was built to support a [Medium article](https://medium.com/@thegoncalomartins/building-a-knowledge-base-service-with-neo4j-kafka-and-the-outbox-pattern-9fffeaa284a6).

## Requirements

* **[Gradle >= 7.0.2](https://gradle.org/releases/)**
* **[JDK >= 11](https://www.oracle.com/java/technologies/downloads/)**
* **[Docker](https://docs.docker.com/get-docker/)**
* **[Postman](https://www.postman.com/downloads/)**

## Documentation

### Domain Model

![](./docs/domain-model.png)

### Architecture

![](./docs/architecture.png)

### Change Data Capture with Outbox Pattern

![](./docs/outbox-pattern-with-cdc.png)

### Services

* ["People" Documentation](./people/README.md)
* ["Movies" Documentation](./movies/README.md)
* ["Knowledge Base" Documentation](./knowledge-base/README.md)


## Running

To run all the services and infrastructure just run:
```bash
$ ./init.sh
```

It may take a while because all the gradle dependencies need to be downloaded and the source code compiled.

After the script has finished, the API Gateway should be exposed at port `8090`.

You can check that by running:
```bash
$ curl http://localhost:8090/__health
{"status":"OK"}
```

## Demo

Open the [postman collection](postman/KnowledgeBaseWithOutboxPattern.postman_collection.json) and try to make some requests.

You can then go to [Kafka's Landoop UI](http://localhost:3030) to check if messages have arrived to `people-db.people.outbox` and `movies-db.movies.outbox` topics.



To check that the information is persited in Neo4j, go to [Neo4j Browser](http://localhost:7474) and run the following query `MATCH (n) RETURN n LIMIT 35`.
It should return some nodes and edges.

Example:

![](./docs/neo4j-screenshot.png)

## Metrics

