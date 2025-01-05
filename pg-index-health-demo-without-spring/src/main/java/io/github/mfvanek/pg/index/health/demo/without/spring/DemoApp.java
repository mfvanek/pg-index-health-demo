/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring;

import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.factory.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.Consts;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.HealthDataCollector;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.MigrationRunner;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.MigrationsGenerator;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.testing.PostgreSqlContainerWrapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@UtilityClass
public class DemoApp {

    @SneakyThrows
    public static void main(final String[] args) {
        try (PostgreSqlContainerWrapper postgres = PostgreSqlContainerWrapper.withVersion(Consts.PG_VERSION)) {
            MigrationRunner.runMigrations(postgres.getDataSource());
            final HighAvailabilityPgConnectionFactory connectionFactory = new HighAvailabilityPgConnectionFactoryImpl(
                new PgConnectionFactoryImpl(), new PrimaryHostDeterminerImpl());
            final ConnectionCredentials credentials = ConnectionCredentials.ofUrl(postgres.getUrl(), postgres.getUsername(), postgres.getPassword());
            HealthDataCollector.collectHealthData(connectionFactory, credentials);
            final List<ForeignKey> foreignKeys = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(connectionFactory, credentials);
            MigrationsGenerator.generateMigrations(postgres.getDataSource(), foreignKeys);
            final List<ForeignKey> afterMigrations = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(connectionFactory, credentials);
            if (!afterMigrations.isEmpty()) {
                throw new IllegalStateException("There should be no foreign keys not covered by the index");
            }
        }
    }
}
