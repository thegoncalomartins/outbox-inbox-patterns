# People

`people` is the service that offers a level three REST API to perform CRUD (Create, Read, Update and Delete) operations for "Person" entity.

## Documentation

### Architecture

![architecture](docs/people-architecture.png)

### Package Diagram

![architecture](docs/people-package-diagram.png)

## Running

### Via Docker

```bash
$ docker-compose up -d api-gateway people-api people-db-primary people-db-secondary people-db-arbiter
```

### Locally

#### Starting dependencies

```bash
$ docker-compose up -d people-db-primary people-db-secondary people-db-arbiter
```

#### Running

```bash
$ ./gradlew clean build quarkusDev -x test
```

## Testing

### Starting dependencies
```bash
$ docker-compose -f ../docker-compose.test.yml up -d test-people-db-primary test-people-db-secondary test-people-db-arbiter
```

### Testing
```bash
$ ./gradlew clean build
```
