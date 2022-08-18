/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster;
import io.github.mfvanek.pg.common.maintenance.AbstractCheckOnCluster;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.DbMigrationGeneratorImpl;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.index.health.demo.utils.HealthDataCollector;
import io.github.mfvanek.pg.index.health.demo.utils.MigrationRunner;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.index.ForeignKey;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.annotation.Nonnull;

@Slf4j
public class DemoApp {

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            MigrationRunner.runMigrations(embeddedPostgres.getPostgresDatabase());
            HealthDataCollector.collectHealthData("postgres", embeddedPostgres.getPort());
            generateMigrations(embeddedPostgres);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void generateMigrations(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final AbstractCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex = new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
        final PgContext context = PgContext.of("demo");
        final List<ForeignKey> foreignKeys = foreignKeysNotCoveredWithIndex.check(context);
        final DbMigrationGenerator generator = new DbMigrationGeneratorImpl();
        final String generatedMigrations = generator.generate(foreignKeys, GeneratingOptions.builder().build());
        log.info(generatedMigrations);
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(generatedMigrations);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        final List<ForeignKey> afterMigrations = foreignKeysNotCoveredWithIndex.check(context);
        if (!afterMigrations.isEmpty()) {
            throw new IllegalStateException("There should be no foreign keys not covered by the index");
        }
    }
}
