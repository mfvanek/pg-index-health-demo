/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfig {

    @SuppressWarnings({"java:S2095", "java:S1452"})
    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer postgreSqlContainer() {
        return new PostgreSQLContainer("postgres:18.0")
            .withDatabaseName("demo_for_pg_index_health")
            .withUsername("demo_user")
            .withPassword("myUniquePassword")
            .waitingFor(Wait.forListeningPort());
    }

    @Bean
    public DataSource dataSource(final PostgreSQLContainer postgreSqlContainer,
                                 final Environment environment) {
        ConfigurableEnvironmentMutator.addDatasourceUrlIfNeed(postgreSqlContainer, environment);
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgreSqlContainer.getJdbcUrl());
        hikariConfig.setUsername(postgreSqlContainer.getUsername());
        hikariConfig.setPassword(postgreSqlContainer.getPassword());
        return new HikariDataSource(hikariConfig);
    }
}
