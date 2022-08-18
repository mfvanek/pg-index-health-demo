/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.utils.MigrationRunner;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

public abstract class DatabaseAwareTestBase {

    @RegisterExtension
    private static final PreparedDbExtension EMBEDDED_POSTGRES = EmbeddedPostgresExtension.preparedDatabase(ds -> {
    });

    @BeforeAll
    static void runMigrations() {
        MigrationRunner.runMigrations(getDataSource());
    }

    @Nonnull
    protected static DataSource getDataSource() {
        return EMBEDDED_POSTGRES.getTestDatabase();
    }

    @Nonnull
    protected static String getDatabaseName() {
        return EMBEDDED_POSTGRES.getConnectionInfo().getDbName();
    }

    protected static int getPort() {
        return EMBEDDED_POSTGRES.getConnectionInfo().getPort();
    }
}
