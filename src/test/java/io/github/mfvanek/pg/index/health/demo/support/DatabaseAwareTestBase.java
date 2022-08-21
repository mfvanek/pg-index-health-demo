/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.support;

import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.index.health.demo.utils.MigrationRunner;
import io.github.mfvanek.pg.index.health.demo.utils.PostgreSqlContainerWrapper;
import org.junit.jupiter.api.BeforeAll;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

public abstract class DatabaseAwareTestBase {

    private static final PostgreSqlContainerWrapper POSTGRES = new PostgreSqlContainerWrapper("13.7");

    @Nonnull
    protected static DataSource getDataSource() {
        return POSTGRES.getDataSource();
    }

    @BeforeAll
    static void runMigrations() {
        MigrationRunner.runMigrations(getDataSource());
    }

    @Nonnull
    protected static ConnectionCredentials getConnectionCredentials() {
        return ConnectionCredentials.ofUrl(POSTGRES.getUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
