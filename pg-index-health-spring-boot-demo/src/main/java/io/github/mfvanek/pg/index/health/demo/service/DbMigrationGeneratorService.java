/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mfvanek.pg.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster;
import io.github.mfvanek.pg.common.maintenance.DatabaseCheckOnCluster;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DbMigrationGeneratorService {

    private final DataSource dataSource;
    private final DbMigrationGenerator<ForeignKey> dbMigrationGenerator;
    private final HighAvailabilityPgConnection haPgConnection;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public ForeignKeyMigrationResponse generateMigrationsWithForeignKeysChecked() {
        final List<ForeignKey> keysBefore = getForeignKeysFromDb();
        final List<String> migrations = generatedMigrations(keysBefore);
        final List<ForeignKey> keysAfter = getForeignKeysFromDb();
        if (!keysAfter.isEmpty()) {
            throw new IllegalStateException("There should be no foreign keys not covered by the index");
        }
        return new ForeignKeyMigrationResponse(keysBefore, keysAfter, migrations);
    }

    public List<ForeignKey> getForeignKeysFromDb() {
        final DatabaseCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex = new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
        return foreignKeysNotCoveredWithIndex.check(PgContext.of("demo"));
    }

    @SuppressFBWarnings("SIL_SQL_IN_LOOP")
    private List<String> generatedMigrations(final List<ForeignKey> foreignKeys) {
        final List<String> generatedMigrations = dbMigrationGenerator.generate(foreignKeys);
        log.info("Generated migrations: {}", generatedMigrations);
        try (Connection connection = dataSource.getConnection()) {
            for (final String migration : generatedMigrations) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(migration);
                }
            }
        } catch (SQLException e) {
            log.error("Error running migration", e);
        }
        return generatedMigrations;
    }
}
