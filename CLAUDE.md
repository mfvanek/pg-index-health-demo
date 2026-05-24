# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## General Rules

- **Never commit changes** unless the user explicitly asks to commit. Make all edits and stop — do not stage or commit on your own initiative.

## Project Overview

Demo project showcasing the [pg-index-health](https://github.com/mfvanek/pg-index-health) library, which identifies and reports common PostgreSQL database structural problems (missing indexes, duplicated indexes, tables without PKs, etc.).
The project has three demo modules that all share the same Liquibase migrations from `db-migrations`.

## Modules

- **`db-migrations`** — shared Liquibase SQL migrations applied by all other modules
- **`pg-index-health-demo-without-spring`** — plain Java, no Spring; uses Apache DBCP2 and Testcontainers directly
- **`pg-index-health-spring-boot-demo`** — Spring Boot 3 + Java; full REST API with actuator, security, OpenAPI
- **`pg-index-health-spring-boot-kotlin-demo`** — Spring Boot 3 + Kotlin; feature-parity with the Java Spring Boot demo

## Build Commands

```bash
# Full build (compiles, runs all checks and tests, mutation testing, coverage verification)
./gradlew build

# Build without mutation testing (faster)
./gradlew check

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :pg-index-health-spring-boot-demo:test
./gradlew :pg-index-health-demo-without-spring:test
./gradlew :pg-index-health-spring-boot-kotlin-demo:test

# Run a single test class
./gradlew :pg-index-health-spring-boot-demo:test --tests "io.github.mfvanek.pg.index.health.demo.DatabaseStructureStaticAnalysisTest"

# Run mutation tests only
./gradlew :pg-index-health-spring-boot-demo:pitest

# Generate JaCoCo coverage report
./gradlew :pg-index-health-spring-boot-demo:jacocoTestReport

# Check for dependency updates
./gradlew dependencyUpdates

# Run the Kotlin Spring Boot demo locally (requires docker-compose first)
docker-compose -f pg-index-health-spring-boot-kotlin-demo/docker-compose.yml up -d
./gradlew :pg-index-health-spring-boot-kotlin-demo:bootRun
```

## Code Quality

The `pg-index-health-demo.java-conventions` plugin stacks these tools on every Java module (they run as part of `test`):

| Tool | Config location |
|------|----------------|
| Checkstyle | `config/checkstyle/checkstyle.xml` |
| PMD | `config/pmd/pmd.xml` |
| SpotBugs + FindSecBugs + sb-contrib | `config/spotbugs/exclude.xml` |
| ErrorProne + NullAway | inline in `java-conventions.gradle.kts` |
| Forbidden APIs | `config/forbidden-apis/forbidden-apis.txt` |

For the Kotlin module (`pg-index-health-spring-boot-kotlin-demo`), **Detekt** replaces Checkstyle/PMD/SpotBugs (config: `config/detekt/detekt.yml`).
Detekt runs automatically as part of `test` with `autoCorrect = true`, so it fixes formatting issues in place.
Run it explicitly:

```bash
./gradlew :pg-index-health-spring-boot-kotlin-demo:detekt
```

**Forbidden patterns to avoid:**
- `Collections.emptyList/emptyMap/emptySet`, `Collections.singleton*`, `Arrays.asList`, `Collections.unmodifiable*` — use `List.of()`, `Map.of()`, `Set.of()` instead
- `org.junit.jupiter.api.Assertions.*` and Hamcrest — use AssertJ exclusively
- `org.junit.jupiter.api.Assertions` in Kotlin (enforced by Detekt's `ForbiddenImport`)

**Null safety:** All Java code must use `@NullMarked` (package-level) or `@NonNull`/`@Nullable` from `org.jspecify`. NullAway is configured to error (not warn).

## Architecture

### Test Infrastructure

All modules use **Testcontainers** with **PostgreSQL 18.0** — no external database is needed for tests. The container is started automatically:

- **Without-Spring module**: `DatabaseAwareTestBase` starts a shared `PostgreSqlContainerWrapper` container in `@BeforeAll` for the entire test class hierarchy.
- **Spring Boot modules**: `DatabaseConfig` bean starts a `PostgreSQLContainer` and wires it as the `DataSource`; tests use `@ActiveProfiles("test")` and extend `BasePgIndexHealthDemoSpringBootTest`.

Migrations are always run via Liquibase before tests execute. The `db-migrations` module holds all SQL under `src/main/resources/db/changelog/sql/`.

### Typical Workflow: Updating pg-index-health Version

Work on this project usually starts by bumping the `pg-index-health-bom` version in `buildSrc/src/main/kotlin/pg-index-health-demo.java-common-deps.gradle.kts`.

New library versions typically introduce new database checks. When that happens:
1. Update the BOM version in `java-common-deps.gradle.kts`
2. Run the tests — `DatabaseStructureStaticAnalysisTest` will fail because the total check count no longer matches `Diagnostic.values().length + CUSTOM_CHECKS_COUNT`
3. Update `DatabaseStructureStaticAnalysisTest` in **all three modules** to expect the new checks and their findings
4. The goal is to make all deviations in the `demo` schema visible — **do not fix the violations**, just update the test assertions to document them

### Key Test: `DatabaseStructureStaticAnalysisTest`

This test (present in all three modules) is the heart of the demo. It:
1. Asserts the PostgreSQL version is `18.0`
2. Verifies the public schema is clean (no issues)
3. Verifies the `demo` schema has *intentionally* introduced problems (invalid index, duplicated indexes, FK without index, tables without PK, etc.)

When adding new intentional DB problems to the `demo` schema, update this test to expect them.

### Custom Checks

Custom database checks extend `AbstractCheckOnHost<T>` and are registered in `CustomChecksConfig` (lives in `src/test/java` or `src/test/kotlin`). Examples:

- `AllPrimaryKeysMustBeNamedAsIdCheckOnHost` — finds PKs not named `id`
- `AllDateTimeColumnsShouldEndWithAtCheckOnHost` — finds timestamp columns not ending in `_at`

Custom checks count as part of the total asserted in `DatabaseStructureStaticAnalysisTest` (`CUSTOM_CHECKS_COUNT = 2`). Increment this constant when adding new custom checks.

### Spring Boot Modules

Both Spring Boot modules expose the same REST API:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/db/health` | GET | Runs all health checks, returns string log lines |
| `/db/migration/generate` | POST | Generates Liquibase migration for FKs without indexes |
| `/db/statistics/reset` | GET | Returns last stats reset timestamp |
| `/db/statistics/reset` | POST | Resets PostgreSQL statistics |

- Main port: **8080**; Actuator port: **8090** (basic auth: `demouser` / `testpwd123`)
- Swagger UI: `http://localhost:8090/actuator/swagger-ui`
- The `pg-index-health-generator` library generates migration SQL; `pg-index-health-logger` logs health data

### Coverage Requirements

Coverage is enforced via `jacocoTestCoverageVerification` and blocks the `check` task on failure:

- `pg-index-health-demo-without-spring`: 92% instruction, 70% branch
- `pg-index-health-spring-boot-demo`: 97% instruction, 95% branch
- `pg-index-health-spring-boot-kotlin-demo`: 97% instruction, 99% branch

### BOM / Version Management

All `pg-index-health-*` library versions are managed via `pg-index-health-bom` in `java-common-deps.gradle.kts`. Testcontainers versions are managed via `testcontainers-bom`. Don't pin individual artifact versions for these — update the BOM version instead.

## Linting (SQLFluff via Super-Linter)

SQL migrations are linted with **SQLFluff** (config: `.github/linters/.sqlfluff`), run through the super-linter Docker image:

Run locally with Docker from the repo root (PowerShell):

```powershell
docker run `
  -e RUN_LOCAL=true `
  -e USE_FIND_ALGORITHM=true `
  -e VALIDATE_SQLFLUFF=true `
  -v "${PWD}:/tmp/lint" `
  ghcr.io/super-linter/super-linter:slim-v8.6.0
```
