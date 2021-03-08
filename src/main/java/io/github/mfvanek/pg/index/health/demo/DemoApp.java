/*
 * Copyright (c) 2019-2021. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.common.health.DatabaseHealthFactory;
import io.github.mfvanek.pg.common.health.DatabaseHealthFactoryImpl;
import io.github.mfvanek.pg.common.health.logger.Exclusions;
import io.github.mfvanek.pg.common.health.logger.HealthLogger;
import io.github.mfvanek.pg.common.health.logger.SimpleHealthLogger;
import io.github.mfvanek.pg.common.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

public class DemoApp {

    public static void main(String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            runMigrations(embeddedPostgres);
            collectHealthData(embeddedPostgres);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void runMigrations(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection()) {
            final DatabaseConnection dbConnection = new JdbcConnection(connection);
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
            final Liquibase liquibase = new Liquibase("changelogs/changelog.xml",
                    new ClassLoaderResourceAccessor(), database);
            liquibase.update("main");
        } catch (SQLException | LiquibaseException e) {
            System.err.println(e.getMessage());
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
        final HealthLogger logger = new SimpleHealthLogger(credentials, connectionFactory, databaseHealthFactory);
        final PgContext context = PgContext.of("demo");
        logger.logAll(exclusions, context)
                .forEach(System.out::println);
    }
}
