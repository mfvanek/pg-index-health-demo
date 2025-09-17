# PostgreSQL Index Health Demo (Kotlin)

A Kotlin Spring Boot demo application showcasing the usage of [pg-index-health](https://github.com/mfvanek/pg-index-health) library for monitoring and maintaining PostgreSQL database health.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Documentation](#documentation)

## Overview

This demo application demonstrates how to integrate the `pg-index-health` library into a Kotlin Spring Boot application to monitor and maintain the health of PostgreSQL databases. The library helps identify common database issues such as missing indexes, unused indexes, and tables without primary keys.

## Features

- Database health monitoring
- Detection of common PostgreSQL issues:
  - Missing indexes on foreign keys
  - Duplicate indexes
  - Unused indexes
  - Tables without primary keys
  - Intersected (partially identical) indexes
  - Tables with [bloat](https://www.percona.com/blog/2018/08/06/basic-understanding-bloat-vacuum-postgresql-mvcc/)
- Automatic generation of database migration scripts
- RESTful API for accessing health information
- Swagger/OpenAPI documentation
- Security with basic authentication

## Getting Started

### Configuration

The application is configured via `application.yaml`. Key configuration properties include:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/demo_for_pg_index_health
    username: demo_user
    password: myUniquePassword

app:
  statistics:
    vacuum-result-polling-attempts: 10
```

### Running the Application

#### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/mfvanek/pg-index-health-demo.git
   cd pg-index-health-demo
   ```

2. Set up the database:
   ```bash
   docker-compose -f pg-index-health-spring-boot-kotlin-demo/docker-compose.yml up -d
   ```

3. Build and run the application:
   ```bash
   ./gradlew :pg-index-health-spring-boot-kotlin-demo:bootRun
   ```

#### Building a JAR

To build a standalone JAR file:

```bash
./gradlew :pg-index-health-spring-boot-kotlin-demo:build
```

The resulting JAR will be located in `pg-index-health-spring-boot-kotlin-demo/build/libs/`.

## API Endpoints

| Endpoint                    | Method | Description                                                |
|-----------------------------|--------|------------------------------------------------------------|
| `/db/health`                | GET    | Collects comprehensive health data from the database       |
| `/db/migration/generate`    | POST   | Generates database migrations with foreign key constraints |
| `/db/statistics/reset`      | GET    | Get last statistics reset date                             |
| `/db/statistics/reset`      | POST   | Resets database statistics                                 |

Protected Actuator endpoints are available on port 8090:
- `/actuator/health`
- `/actuator/prometheus`
- `/actuator/info`

### Authentication

The application uses basic authentication:
- Username: `demouser`
- Password: `testpwd123`

## Testing

Run unit and integration tests:

```bash
./gradlew :pg-index-health-spring-boot-kotlin-demo:test
```

Generate test coverage report:

```bash
./gradlew :pg-index-health-spring-boot-kotlin-demo:jacocoTestReport
```

## Documentation

API documentation is available via Swagger UI:

- http://localhost:8080/swagger-ui.html

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://github.com/mfvanek/pg-index-health-demo/blob/master/LICENSE) file for details.
