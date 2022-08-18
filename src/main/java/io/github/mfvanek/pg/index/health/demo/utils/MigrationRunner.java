/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

@Slf4j
@UtilityClass
public final class MigrationRunner {

    public static void runMigrations(@Nonnull final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             DatabaseConnection dbConnection = new JdbcConnection(connection)) {
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
            try (Liquibase liquibase = new Liquibase("changelogs/changelog.xml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("main");
            }
        } catch (SQLException | LiquibaseException e) {
            log.error(e.getMessage(), e);
        }
    }
}
