/*
 * Copyright (c) 2019-2023. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

@Slf4j
@UtilityClass
public final class MigrationRunner {

    @SuppressWarnings("deprecation")
    @SneakyThrows
    public static void runMigrations(@Nonnull final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            final Map<String, Object> config = new HashMap<>();
            Scope.child(config, () -> {
                final DatabaseConnection dbConnection = new JdbcConnection(connection);
                final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
                final Liquibase liquibase = new Liquibase("changelogs/changelog.xml", new ClassLoaderResourceAccessor(), database);
                liquibase.update("main");
            });
            log.info("Migrations have been successfully executed");
        }
    }
}
