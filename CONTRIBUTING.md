# Contributing to pg-index-health-demo

Thank you for your interest in contributing to the pg-index-health-demo project! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Code Contributions](#code-contributions)
- [Development Setup](#development-setup)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
- [Coding Standards](#coding-standards)
  - [Java Code Conventions](#java-code-conventions)
  - [Kotlin Code Conventions](#kotlin-code-conventions)
  - [SQL Conventions](#sql-conventions)
- [Project Structure](#project-structure)
- [Testing](#testing)
  - [Unit Tests](#unit-tests)
  - [Integration Tests](#integration-tests)
- [Pull Request Process](#pull-request-process)
- [License](#license)

## Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## How Can I Contribute?

### Reporting Bugs

Before submitting a bug report, please check if the issue has already been reported. If not, create a new issue with the following information:

1. A clear and descriptive title
2. A detailed description of the problem
3. Steps to reproduce the issue
4. Expected behavior vs. actual behavior
5. Environment information (OS, Java version, etc.)
6. Any relevant logs or error messages

### Suggesting Enhancements

Enhancement suggestions are welcome! Please create an issue with:

1. A clear and descriptive title
2. A detailed explanation of the proposed enhancement
3. The motivation behind the enhancement
4. Any implementation ideas or examples

### Code Contributions

We welcome code contributions via pull requests. Follow these steps:

1. Fork the repository
2. Create a new branch for your feature or bugfix
3. Make your changes
4. Add or update tests as necessary
5. Ensure all tests pass
6. Submit a pull request with a clear description of your changes

## Development Setup

### Prerequisites

- Java 21 or higher
- Gradle 8.x
- Docker (for running PostgreSQL with Testcontainers)
- Git

### Building the Project

This is a multi-module Gradle project. To build all modules:

```bash
./gradlew build
```

To build a specific module:

```bash
./gradlew :pg-index-health-spring-boot-kotlin-demo:build
```

### Running Tests

To run all tests:

```bash
./gradlew test
```

To run tests for a specific module:

```bash
./gradlew :pg-index-health-spring-boot-demo:test
```

To generate test coverage reports:

```bash
./gradlew jacocoTestReport
```

## Coding Standards

### Java Code Conventions

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Write Javadoc for public classes and methods
- Keep methods short and focused on a single responsibility
- Avoid unnecessary comments that just restate what the code does

### Kotlin Code Conventions

- Follow official Kotlin coding conventions
- Use idiomatic Kotlin where appropriate
- Leverage Kotlin features like extension functions, data classes, etc.
- Write KDoc for public classes and methods
- Use meaningful variable and function names

### SQL Conventions

- Use lowercase for SQL keywords
- Use snake_case for table and column names
- Write clear, well-formatted SQL queries
- Include comments for complex queries

## Project Structure

The project consists of several modules:

1. `pg-index-health-demo-without-spring` - Demo without Spring/Spring Boot
2. `pg-index-health-spring-boot-demo` - Java Spring Boot demo
3. `pg-index-health-spring-boot-kotlin-demo` - Kotlin Spring Boot demo
4. `db-migrations` - Database migration scripts

Each module demonstrates different aspects of the pg-index-health library integration.

## Testing

### Unit Tests

Unit tests should:
- Focus on a single class or method
- Mock external dependencies
- Run quickly
- Be independent of other tests
- Cover edge cases and error conditions

### Integration Tests

Integration tests should:
- Test the interaction between components
- Use Testcontainers for database integration
- Verify end-to-end functionality
- Be placed in appropriately named test classes

## Pull Request Process

1. Ensure your code follows the project's coding standards
2. Add or update tests for your changes
3. Update documentation if necessary
4. Run all tests to ensure nothing is broken
5. Squash related commits for clarity
6. Submit a pull request with a clear description of the changes
7. Address any feedback during the review process

## License

By contributing to this project, you agree that your contributions will be licensed under the Apache License 2.0.
