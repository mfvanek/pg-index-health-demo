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
import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationRequest;
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

    private final HighAvailabilityPgConnectionFactory highAvailabilityPgConnectionFactory;
    private final DataSource dataSource;

    public ForeignKeyMigrationResponse addIndexesWithFkChecks(final ForeignKeyMigrationRequest fkMigrationRequest) {
        final List<ForeignKey> keysBefore = getFksFromDb(fkMigrationRequest.credentials());
        final List<String> migrations = generatedMigrations(keysBefore);
        final List<ForeignKey> keysAfter = getFksFromDb(fkMigrationRequest.credentials());
        if (!keysAfter.isEmpty()) {
            throw new IllegalStateException("There should be no foreign keys not covered by the index");
        }
        return new ForeignKeyMigrationResponse(keysBefore, keysAfter, migrations);
    }

    private List<ForeignKey> getFksFromDb(final ConnectionCredentials credentials) {
        final HighAvailabilityPgConnection haPgConnection = highAvailabilityPgConnectionFactory.of(credentials);
        final DatabaseCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex = new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
        return foreignKeysNotCoveredWithIndex.check(PgContext.of("demo"));
    }

    @SuppressFBWarnings("SIL_SQL_IN_LOOP")
    private List<String> generatedMigrations(final List<ForeignKey> foreignKeys) {
        final DbMigrationGenerator<ForeignKey> generator = new ForeignKeyMigrationGenerator(GeneratingOptions.builder().build());
        final List<String> generatedMigrations = generator.generate(foreignKeys);
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
