/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.common.health.DatabaseHealth;
import io.github.mfvanek.pg.common.health.DatabaseHealthFactory;
import io.github.mfvanek.pg.common.health.DatabaseHealthFactoryImpl;
import io.github.mfvanek.pg.common.health.DatabaseHealthImpl;
import io.github.mfvanek.pg.common.health.logger.Exclusions;
import io.github.mfvanek.pg.common.health.logger.HealthLogger;
import io.github.mfvanek.pg.common.health.logger.KeyValueFileHealthLogger;
import io.github.mfvanek.pg.common.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.DbMigrationGeneratorImpl;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.index.ForeignKey;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nonnull;

public class DemoApp {

    private static final Logger logger = LoggerFactory.getLogger(DemoApp.class);

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            runMigrations(embeddedPostgres);
            collectHealthData(embeddedPostgres);
            generateMigrations(embeddedPostgres);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void runMigrations(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection()) {
            final DatabaseConnection dbConnection = new JdbcConnection(connection);
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
            try (Liquibase liquibase = new Liquibase("changelogs/changelog.xml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("main");
            }
        } catch (SQLException | LiquibaseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void collectHealthData(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        final String url = String.format("jdbc:postgresql://localhost:%d/postgres", embeddedPostgres.getPort());
        final ConnectionCredentials credentials = ConnectionCredentials.ofUrl(url, "postgres", "postgres");
        final HighAvailabilityPgConnectionFactory connectionFactory = new HighAvailabilityPgConnectionFactoryImpl(
                new PgConnectionFactoryImpl(), new PrimaryHostDeterminerImpl());
        final DatabaseHealthFactory databaseHealthFactory = new DatabaseHealthFactoryImpl(new MaintenanceFactoryImpl());
        final Exclusions exclusions = Exclusions.builder()
                .withIndexSizeThreshold(1, MemoryUnit.MB)
                .withTableSizeThreshold(1, MemoryUnit.MB)
                .build();
        final HealthLogger healthLogger = new KeyValueFileHealthLogger(credentials, connectionFactory, databaseHealthFactory);
        final PgContext context = PgContext.of("demo");
        healthLogger.logAll(exclusions, context)
                .forEach(logger::info);
    }

    private static void generateMigrations(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final DatabaseHealth databaseHealth = new DatabaseHealthImpl(haPgConnection, new MaintenanceFactoryImpl());
        final PgContext context = PgContext.of("demo");
        final List<ForeignKey> foreignKeys = databaseHealth.getForeignKeysNotCoveredWithIndex(context);
        final DbMigrationGenerator generator = new DbMigrationGeneratorImpl();
        logger.info(generator.generate(foreignKeys, GeneratingOptions.builder().build()));
        // TODO apply migrations and run check again
    }
}
