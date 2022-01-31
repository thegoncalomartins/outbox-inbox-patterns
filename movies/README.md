# Movies

`movies` is the service that offers a level three REST API to perform CRUD (Create, Read, Update and Delete) operations for "Movie" entity.

## Documentation

### Architecture

![architecture](docs/movies-architecture.png)

### Package Diagram

![package-diagram](docs/movies-package-diagram.png)

## Running

### Via Docker

```bash
$ docker-compose up -d api-gateway movies-api movies-db-primary movies-db-secondary movies-db-arbiter
```

### Locally

#### Starting dependencies

```bash
$ docker-compose up -d movies-db-primary movies-db-secondary movies-db-arbiter
```

#### Running

```bash
$ ./gradlew clean build quarkusDev -x test
```

## Testing

### Starting dependencies
```bash
$ docker-compose -f ../docker-compose.test.yml up -d test-movies-db-primary test-movies-db-secondary test-movies-db-arbiter
```

### Testing
```bash
$ ./gradlew clean build
```

## Debezium MongoDB Kafka Connector Configuration

```yaml
connector.class: io.debezium.connector.mongodb.MongoDbConnector
key.converter: org.apache.kafka.connect.storage.StringConverter
value.converter: org.apache.kafka.connect.json.JsonConverter
transforms: unwrap,createKey
mongodb.hosts: movies-db-primary:27017
mongodb.user: root
mongodb.password: xH8Tw7Ug8ApgEC2b
mongodb.name: movies-db
collection.include.list: movies.outbox
transforms.createKey.type: org.apache.kafka.connect.transforms.ValueToKey
transforms.addPrefix.type: org.apache.kafka.connect.transforms.RegexRouter
transforms.createKey.fields: aggregate_id
transforms.unwrap.type: io.debezium.connector.mongodb.transforms.ExtractNewDocumentState
key.converter.schemas.enable: false
value.converter.schemas.enable: false
```
