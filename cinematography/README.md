# Cinematography

`cinematography` is the service that aggregates and builds the knowledge graph of the system. It offers a level three REST API
to query the graph.

## Documentation

### Architecture

## Running

### Via Docker

```bash
$ docker-compose up -d api-gateway cinematography-api cinematography-consumer kafka cinematography-db
```

### Locally

#### API

```bash
$ ./gradlew clean build :web:quarkusDev -x test
```

#### Consumer

```bash
$ ./gradlew clean build :consumer:quarkusDev -x test
```

## Testing

### Starting dependencies
```bash
$ docker-compose up -d test-kafka test-cinematography-db
```

### Testing common
```bash
$ cd common && ./gradlew clean build
```

### Testing API
```bash
$ cd web && ./gradlew clean build
```

### Testing Consumer
```bash
$ cd consumer && ./gradlew clean build
```
