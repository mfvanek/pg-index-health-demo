/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import javax.sql.DataSource

/**
 * Configuration class for database connectivity.
 *
 * This configuration provides a PostgreSQL database connection using Testcontainers
 * for testing purposes and HikariCP for connection pooling in production.
 *
 * The configuration dynamically sets up the datasource URL using [ConfigurableEnvironmentMutator]
 * when running with Testcontainers, making it suitable for both local development
 * and testing scenarios.
 *
 * TODO: move to test directory?
 *
 * @see ConfigurableEnvironmentMutator
 * @see PostgreSQLContainer
 * @see HikariDataSource
 */
@Configuration(proxyBeanMethods = false)
class DatabaseConfig {

    /**
     * Creates and configures a PostgreSQL Testcontainer.
     *
     * This bean provides a PostgreSQL database instance for testing purposes.
     * The container is automatically started and stopped with the application.
     *
     * @return configured [PostgreSQLContainer] instance
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun jdbcDatabaseContainer(): JdbcDatabaseContainer<*> {
        return PostgreSQLContainer("postgres:17.4")
            .withDatabaseName("demo_for_pg_index_health")
            .withUsername("demo_user")
            .withPassword("myUniquePassword")
            .waitingFor(Wait.forListeningPort())
    }

    /**
     * Creates and configures a Hikari datasource.
     *
     * This bean provides a connection pool to the database. When running with
     * Testcontainers, it uses [ConfigurableEnvironmentMutator] to dynamically
     * configure the datasource URL.
     *
     * @param jdbcDatabaseContainer the database container (from Testcontainers)
     * @param environment the Spring environment
     * @return configured [HikariDataSource] instance
     * @see ConfigurableEnvironmentMutator
     */
    @Bean
    fun dataSource(
        jdbcDatabaseContainer: JdbcDatabaseContainer<*>,
        environment: Environment
    ): DataSource {
        ConfigurableEnvironmentMutator.addDatasourceUrlIfNeed(jdbcDatabaseContainer, environment)
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = jdbcDatabaseContainer.jdbcUrl
        hikariConfig.username = jdbcDatabaseContainer.username
        hikariConfig.password = jdbcDatabaseContainer.password
        return HikariDataSource(hikariConfig)
    }
}
