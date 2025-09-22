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
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
class TestContainersConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun jdbcDatabaseContainer(): JdbcDatabaseContainer<*> {
        return PostgreSQLContainer("postgres:17.4")
    }

    @Bean
    fun connectionCredentialsConfig(jdbcDatabaseContainer: JdbcDatabaseContainer<*>): ConnectionCredentialsConfig {
        return ConnectionCredentialsConfig(
            jdbcDatabaseContainer.jdbcUrl,
            jdbcDatabaseContainer.username,
            jdbcDatabaseContainer.password
        )
    }

    @Bean
    fun dataSource(jdbcDatabaseContainer: JdbcDatabaseContainer<*>): DataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = jdbcDatabaseContainer.jdbcUrl
        hikariConfig.username = jdbcDatabaseContainer.username
        hikariConfig.password = jdbcDatabaseContainer.password
        return HikariDataSource(hikariConfig)
    }
}
