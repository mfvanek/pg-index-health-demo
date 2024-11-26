/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.factory.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.core.settings.ConfigurationMaintenanceOnHostImpl;
import io.github.mfvanek.pg.core.statistics.StatisticsMaintenanceOnHostImpl;
import io.github.mfvanek.pg.health.checks.management.DatabaseManagement;
import io.github.mfvanek.pg.health.checks.management.DatabaseManagementImpl;
import io.github.mfvanek.pg.health.logger.DatabaseChecksOnCluster;
import io.github.mfvanek.pg.health.logger.HealthLogger;
import io.github.mfvanek.pg.health.logger.StandardHealthLogger;
import io.github.mfvanek.pg.model.context.PgContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.annotation.Nonnull;

@Configuration(proxyBeanMethods = false)
public class DatabaseStructureHealthConfig {

    @Bean
    public ConnectionCredentials connectionCredentials(@Nonnull final JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        return ConnectionCredentials.ofUrl(jdbcDatabaseContainer.getJdbcUrl(),
            jdbcDatabaseContainer.getUsername(), jdbcDatabaseContainer.getPassword());
    }

    @Bean
    public HighAvailabilityPgConnectionFactory highAvailabilityPgConnectionFactory() {
        return new HighAvailabilityPgConnectionFactoryImpl(new PgConnectionFactoryImpl(), new PrimaryHostDeterminerImpl());
    }

    @Bean
    public HealthLogger healthLogger(@Nonnull final ConnectionCredentials connectionCredentials,
                                     @Nonnull final HighAvailabilityPgConnectionFactory highAvailabilityPgConnectionFactory) {
        return new StandardHealthLogger(connectionCredentials, highAvailabilityPgConnectionFactory, DatabaseChecksOnCluster::new);
    }

    @Bean
    public HighAvailabilityPgConnection highAvailabilityPgConnection(
        @Nonnull final ConnectionCredentials connectionCredentials,
        @Nonnull final HighAvailabilityPgConnectionFactory highAvailabilityPgConnectionFactory) {
        return highAvailabilityPgConnectionFactory.of(connectionCredentials);
    }

    @Bean
    public DatabaseManagement databaseManagement(@Nonnull final HighAvailabilityPgConnection highAvailabilityPgConnection) {
        return new DatabaseManagementImpl(highAvailabilityPgConnection,
            StatisticsMaintenanceOnHostImpl::new,
            ConfigurationMaintenanceOnHostImpl::new);
    }

    @Bean
    public PgContext pgContext() {
        return PgContext.of("demo");
    }
}
