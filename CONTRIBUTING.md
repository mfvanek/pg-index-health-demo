# Contributing to pg-index-health-demo

Thank you for your interest in contributing to the pg-index-health-demo project! This document provides guidelines and instructions for contributing.

## Table of Contents
- [Building and testing](#building-and-testing)
- [Implementing new features](#implementing-new-features)
- [Code of Conduct](#code-of-conduct)
- [License](#license)

## Building and testing

Java >= 21 is required.

1. Clone the repository

       git clone https://github.com/mfvanek/pg-index-health-demo.git
       cd pg-index-health-demo

2. Build with Gradle
    * On Linux and macOS: `./gradlew build`
    * On Windows: `.\gradlew.bat build`
    
   This will build the project and run tests.  
   **You need to have [Docker](https://www.docker.com/) up and running**.
    
By default, [PostgreSQL from Testcontainers](https://www.testcontainers.org/) is used to run tests.  

## Implementing new features

This is a demo project showcasing the [pg-index-health](https://github.com/mfvanek/pg-index-health) library. 
New features typically involve:

### Extending existing demo modules

The project consists of several modules:
1. `pg-index-health-demo-without-spring` - Demo without Spring/Spring Boot
2. `pg-index-health-spring-boot-demo` - Java Spring Boot demo
3. `pg-index-health-spring-boot-kotlin-demo` - Kotlin Spring Boot demo

When adding new features, consider which module is most appropriate for your contribution.

### Adding new endpoints or services

1. Follow the existing code structure and patterns
2. Add appropriate tests (unit and integration)
3. Update documentation in README.md files
4. Ensure code follows the established conventions:
   * Java code follows standard Java naming conventions
   * Kotlin code follows official Kotlin coding conventions
   * SQL queries use lowercase keywords and snake_case for identifiers

### Write proper tests

* Your code must be adequately covered by tests
* Integration tests should use Testcontainers for database integration
* Behavior should be tested with different scenarios
* Tests should be placed in appropriately named test classes

## Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## License

By contributing to this project, you agree that your contributions will be licensed under the Apache License 2.0.
