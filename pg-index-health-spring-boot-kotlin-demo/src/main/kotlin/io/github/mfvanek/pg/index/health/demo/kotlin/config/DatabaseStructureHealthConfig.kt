/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection
import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactoryImpl
import io.github.mfvanek.pg.connection.factory.PgConnectionFactoryImpl
import io.github.mfvanek.pg.core.statistics.StatisticsMaintenanceOnHostImpl
import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.health.checks.management.DatabaseManagementImpl
import io.github.mfvanek.pg.health.logger.DatabaseChecksOnCluster
import io.github.mfvanek.pg.health.logger.HealthLogger
import io.github.mfvanek.pg.health.logger.StandardHealthLogger
import io.github.mfvanek.pg.model.context.PgContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.JdbcDatabaseContainer

// TODO: look at pg-index-health-jdbc-connection lib
@Configuration(proxyBeanMethods = false)
class DatabaseStructureHealthConfig {

    @Bean
    fun connectionCredentials(jdbcDatabaseContainer: JdbcDatabaseContainer<*>): ConnectionCredentials {
        return ConnectionCredentials.ofUrl(
            jdbcDatabaseContainer.jdbcUrl,
            jdbcDatabaseContainer.username,
            jdbcDatabaseContainer.password
        )
    }

    @Bean
    fun highAvailabilityPgConnectionFactory(): HighAvailabilityPgConnectionFactory {
        return HighAvailabilityPgConnectionFactoryImpl(
            PgConnectionFactoryImpl(),
            PrimaryHostDeterminerImpl()
        )
    }

    @Bean
    fun healthLogger(
        connectionCredentials: ConnectionCredentials,
        highAvailabilityPgConnectionFactory: HighAvailabilityPgConnectionFactory
    ): HealthLogger {
        return StandardHealthLogger(
            connectionCredentials,
            highAvailabilityPgConnectionFactory
        ) { connection -> DatabaseChecksOnCluster(connection) }
    }

    @Bean
    fun highAvailabilityPgConnection(
        connectionCredentials: ConnectionCredentials,
        highAvailabilityPgConnectionFactory: HighAvailabilityPgConnectionFactory
    ): HighAvailabilityPgConnection {
        return highAvailabilityPgConnectionFactory.of(connectionCredentials)
    }

    @Bean
    fun databaseManagement(highAvailabilityPgConnection: HighAvailabilityPgConnection): DatabaseManagement {
        return DatabaseManagementImpl(highAvailabilityPgConnection) { connection ->
            StatisticsMaintenanceOnHostImpl(connection)
        }
    }

    @Bean
    fun pgContext(): PgContext {
        return PgContext.of("demo")
    }
}
