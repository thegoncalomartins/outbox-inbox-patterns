# Movies

`movies` is the service that offers a level three REST API to perform CRUD (Create, Read, Update and Delete) operations for "Movie" entity.

## Documentation

### Architecture

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
$ docker-compose -f docker-compose.test.yml up -d test-movies-db-primary test-movies-db-secondary test-movies-db-arbiter
```

### Testing
```bash
$ ./gradlew clean build
```
