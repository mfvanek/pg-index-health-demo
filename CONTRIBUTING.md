# Contributing to pg-index-health-demo

Thank you for your interest in contributing! This guide will help you get started.

## Prerequisites

- **JDK 21** (e.g. [Eclipse Temurin](https://adoptium.net/))
- **Docker** — required for tests (Testcontainers spins up PostgreSQL automatically) and for running the SQL linter locally
- **Git**

No external PostgreSQL installation is needed — all tests are self-contained via Testcontainers.

## Getting Started

```bash
git clone https://github.com/mfvanek/pg-index-health-demo.git
cd pg-index-health-demo
./gradlew check   # compile + all static analysis + tests (no mutation testing)
```

`./gradlew build` also runs mutation tests (Pitest) and takes considerably longer.

## Project Structure

| Module                                    | Language | Purpose                                              |
|-------------------------------------------|----------|------------------------------------------------------|
| `db-migrations`                           | SQL      | Shared Liquibase migrations used by all demo modules |
| `pg-index-health-demo-without-spring`     | Java     | Standalone demo, no Spring                           |
| `pg-index-health-spring-boot-demo`        | Java     | Spring Boot 3 REST API demo                          |
| `pg-index-health-spring-boot-kotlin-demo` | Kotlin   | Kotlin equivalent of the Spring Boot demo            |

## Making Changes

### Adding or modifying SQL migrations

- Add SQL files under `db-migrations/src/main/resources/db/changelog/sql/` and register them in `db.changelog-master.yaml`.
- Run the SQLFluff linter after any SQL change (see [Linting SQL](#linting-sql)).
- The `demo` schema intentionally contains database problems (missing indexes, duplicated indexes, etc.) to showcase the library.  
  **Do not fix those violations** — if your migration introduces a new intentional problem, update `DatabaseStructureStaticAnalysisTest` in all three modules to expect it.

### Adding a new custom database check

1. Create a class extending `AbstractCheckOnHost<T>` in the appropriate `checks/custom/` package.
2. Register it in `CustomChecksConfig` (located in `src/test/java` or `src/test/kotlin`).
3. Increment `CUSTOM_CHECKS_COUNT` in `DatabaseStructureStaticAnalysisTest` for that module.
4. Add the expected findings to the `databaseStructureCheckForDemoSchema` switch statement.

### Updating the pg-index-health library version

1. Bump the BOM version in `buildSrc/src/main/kotlin/pg-index-health-demo.java-common-deps.gradle.kts`.
2. Run `./gradlew test` — `DatabaseStructureStaticAnalysisTest` may fail if new checks were added.
3. Update `DatabaseStructureStaticAnalysisTest` in **all three modules** to reflect the new checks.
4. Once tests pass, run `./gradlew pitest` to confirm mutation coverage is still met.

## Code Style

All style checks run automatically as part of `./gradlew test`.

### Java

- Checkstyle, PMD, SpotBugs (with FindSecBugs and sb-contrib), and ErrorProne/NullAway all enforce style and correctness.
- All packages must have a `package-info.java` with `@NullMarked`. Use `@NonNull`/`@Nullable` from `org.jspecify` for individual annotations.
- Use `List.of()`, `Map.of()`, `Set.of()` — `Collections.emptyList()`, `Arrays.asList()`, and similar are forbidden.
- Use **AssertJ** in tests — JUnit `Assertions.*` and Hamcrest are forbidden.

### Kotlin

- Detekt runs automatically with `autoCorrect = true` (it rewrites files in place). Run it explicitly with `./gradlew :pg-index-health-spring-boot-kotlin-demo:detekt`.
- Use AssertJ in tests — Kotlin's built-in `assert`, JUnit Assertions, and Hamcrest are all forbidden.

### SQL

SQL migrations are linted with SQLFluff (Postgres dialect). Config: `.github/linters/.sqlfluff`.

## Linting SQL

Run the SQLFluff linter locally via the super-linter Docker image from the repo root (PowerShell):

```powershell
docker run `
  -e RUN_LOCAL=true `
  -e USE_FIND_ALGORITHM=true `
  -e VALIDATE_SQLFLUFF=true `
  -v "${PWD}:/tmp/lint" `
  ghcr.io/super-linter/super-linter:slim-v8.6.0
```

## Running Tests

```bash
# All modules
./gradlew test

# Single module
./gradlew :pg-index-health-spring-boot-demo:test

# Single test class
./gradlew :pg-index-health-spring-boot-demo:test --tests "io.github.mfvanek.pg.index.health.demo.DatabaseStructureStaticAnalysisTest"
```

Tests run sequentially within each module (`maxParallelForks = 1`) and have one automatic retry on failure.

## Coverage

JaCoCo coverage is verified automatically after each test run and will fail the build if thresholds are not met.

Every new class and method must be covered by tests. Pitest (mutation testing) requires 100% mutation score — `./gradlew build` runs it; `./gradlew :module:pitest` runs it for a single module.

## Submitting a Pull Request

1. Fork the repository and create a branch from `master`.
2. Make your changes and ensure `./gradlew check` passes locally.
3. Run mutation tests for the affected module(s).
4. Open a pull request against `master`. CI runs the full build including mutation tests.

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). Please report unacceptable behavior to mfvanek@gmail.com.
