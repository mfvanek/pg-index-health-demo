/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.service;

import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.model.context.PgContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DbMigrationGeneratorService {

    private final DataSource dataSource;
    private final DbMigrationGenerator<ForeignKey> dbMigrationGenerator;
    private final DatabaseCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex;
    private final PgContext pgContext;

    public ForeignKeyMigrationResponse generateMigrationsWithForeignKeysChecked() {
        final List<ForeignKey> keysBefore = getForeignKeysFromDb();
        final List<String> migrations = generateMigrations(keysBefore);
        runGeneratedMigrations(migrations);
        final List<ForeignKey> keysAfter = getForeignKeysFromDb();
        if (!keysAfter.isEmpty()) {
            throw new IllegalStateException("There should be no foreign keys not covered by the index");
        }
        return new ForeignKeyMigrationResponse(keysBefore, keysAfter, migrations);
    }

    List<ForeignKey> getForeignKeysFromDb() {
        return foreignKeysNotCoveredWithIndex.check(pgContext);
    }

    private List<String> generateMigrations(final List<ForeignKey> foreignKeys) {
        final List<String> generatedMigrations = dbMigrationGenerator.generate(foreignKeys);
        log.info("Generated migrations: {}", generatedMigrations);
        return generatedMigrations;
    }

    private void runGeneratedMigrations(final List<String> generatedMigrations) {
        try (Connection connection = dataSource.getConnection()) {
            for (final String migration : generatedMigrations) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(migration);
                }
            }
        } catch (SQLException e) {
            log.error("Error running migration", e);
        }
    }
}
