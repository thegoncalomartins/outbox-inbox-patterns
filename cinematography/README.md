# cinematography Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/cinematography-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin

## Debezium MongoDB Kafka Connector Configuration

```yaml
connector.class: io.debezium.connector.mongodb.MongoDbConnector
key.converter: org.apache.kafka.connect.storage.StringConverter
value.converter: org.apache.kafka.connect.json.JsonConverter
transforms: unwrap,createKey
mongodb.hosts: cinematography-db-primary:27017
mongodb.user: root
mongodb.password: v5au8MVCvgh5BpSJ
mongodb.name: cinematography-db
collection.include.list: cinematography.outbox
transforms.createKey.type: org.apache.kafka.connect.transforms.ValueToKey
transforms.addPrefix.type: org.apache.kafka.connect.transforms.RegexRouter
transforms.createKey.fields: aggregate_id
transforms.unwrap.type: io.debezium.connector.mongodb.transforms.ExtractNewDocumentState
key.converter.schemas.enable: false
value.converter.schemas.enable: false
```
