/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.support;

import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.factory.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.host.PgHost;
import io.github.mfvanek.pg.connection.host.PgHostImpl;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.Consts;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.MigrationRunner;
import io.github.mfvanek.pg.testing.PostgreSqlContainerWrapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;

public abstract class DatabaseAwareTestBase {

    private static final PostgreSqlContainerWrapper POSTGRES = PostgreSqlContainerWrapper.withVersion(Consts.PG_VERSION);

    @NonNull
    protected static DataSource getDataSource() {
        return POSTGRES.getDataSource();
    }

    @NonNull
    protected static PgHost getHost() {
        return PgHostImpl.ofUrl(POSTGRES.getUrl());
    }

    @NonNull
    protected static String getUrl() {
        return POSTGRES.getUrl();
    }

    @BeforeAll
    static void runMigrations() {
        MigrationRunner.runMigrations(getDataSource());
    }

    @NonNull
    protected static ConnectionCredentials getConnectionCredentials() {
        return ConnectionCredentials.ofUrl(POSTGRES.getUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @NonNull
    protected static HighAvailabilityPgConnectionFactory getConnectionFactory() {
        return new HighAvailabilityPgConnectionFactoryImpl(
            new PgConnectionFactoryImpl(),
            new PrimaryHostDeterminerImpl());
    }
}
