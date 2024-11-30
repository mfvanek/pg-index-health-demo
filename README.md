# pg-index-health-demo

Demo project for [pg-index-health](https://github.com/mfvanek/pg-index-health) library.

[![Java CI](https://github.com/mfvanek/pg-index-health-demo/workflows/Java%20CI/badge.svg)](https://github.com/mfvanek/pg-index-health-demo/actions "Java CI")
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/mfvanek/pg-index-health-demo/blob/master/LICENSE "Apache License 2.0")
[![codecov](https://codecov.io/gh/mfvanek/pg-index-health-demo/branch/master/graph/badge.svg?token=TA13I5NCK4)](https://codecov.io/gh/mfvanek/pg-index-health-demo)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=bugs)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=coverage)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)

[![Mutation testing badge](https://img.shields.io/endpoint?style=flat&url=https%3A%2F%2Fbadge-api.stryker-mutator.io%2Fgithub.com%2Fmfvanek%2Fpg-index-health-demo%2Fmaster)](https://dashboard.stryker-mutator.io/reports/github.com/mfvanek/pg-index-health-demo/master)

## Modules

* [pg-index-health-demo-without-spring](pg-index-health-demo-without-spring)
  * Without Spring/Spring Boot
* [pg-index-health-spring-boot-demo](pg-index-health-spring-boot-demo)
  * With Spring Boot

## Tech stack

* Java 21
* Testcontainers

### Previous versions

If you search for version with [Embedded PostgreSQL](https://github.com/mfvanek/pg-index-health-demo/blob/4269907dc3e5be92fbe90346755bd107260c0c55/src/main/java/io/github/mfvanek/pg/index/health/demo/DemoApp.java#L28)
please take a look at release [0.6.1](https://github.com/mfvanek/pg-index-health-demo/releases/tag/v.0.6.1)  
If you search for Java 8 compatible example please take a look at release [0.6.2](https://github.com/mfvanek/pg-index-health-demo/releases/tag/v.0.6.2)

## Advantages

With **pg-index-health** library you will be able to:
1. [collect indexes health data](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/DemoApp.java) in production environment;
2. [analyze your database structure in functional tests](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/test/java/io/github/mfvanek/pg/index/health/demo/IndexesMaintenanceTest.java) and prevent many of typical errors;
3. [analyze your database configuration](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/ConfigurationDemoApp.java) to prevent using of default values for important options;
4. [manage your database statistics](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/StatisticsDemoApp.java) in production environment;
5. [generate migrations](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/DemoApp.java) to fix your database structure.

## Local development

### Linting

#### macOS/Linux

To run super-linter locally:

```shell
docker run \
  -e RUN_LOCAL=true \
  -e USE_FIND_ALGORITHM=true \
  -e VALIDATE_SQLFLUFF=true \
  -v $(pwd):/tmp/lint \
  ghcr.io/super-linter/super-linter:slim-v7.1.0
```

#### Windows

Use `cmd` on Windows:

```shell
docker run ^
  -e RUN_LOCAL=true ^
  -e USE_FIND_ALGORITHM=true ^
  -e VALIDATE_SQLFLUFF=true ^
  -v "%cd%":/tmp/lint ^
  ghcr.io/super-linter/super-linter:slim-v7.1.0
```
