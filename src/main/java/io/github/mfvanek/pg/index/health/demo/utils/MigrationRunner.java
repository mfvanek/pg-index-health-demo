/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

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
import javax.annotation.Nonnull;

public final class MigrationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationRunner.class);

    private MigrationRunner() {
        throw new UnsupportedOperationException();
    }

    public static void runMigrations(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection();
             DatabaseConnection dbConnection = new JdbcConnection(connection)) {
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
            try (Liquibase liquibase = new Liquibase("changelogs/changelog.xml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("main");
            }
        } catch (SQLException | LiquibaseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
