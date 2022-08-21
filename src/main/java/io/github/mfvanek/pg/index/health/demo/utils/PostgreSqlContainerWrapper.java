/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import org.apache.commons.dbcp2.BasicDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

public class PostgreSqlContainerWrapper implements AutoCloseable {

    private final PostgreSQLContainer<?> container;
    private final DataSource dataSource;

    public PostgreSqlContainerWrapper(@Nonnull final String pgVersion) {
        this(new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag(pgVersion))
                .withSharedMemorySize(512L * 1024L * 1024L)
                .withTmpFs(Collections.singletonMap("/var/lib/postgresql/data", "rw")));
    }

    PostgreSqlContainerWrapper(@Nonnull final PostgreSQLContainer<?> container) {
        this.container = Objects.requireNonNull(container);
        this.container.start();
        this.dataSource = buildDataSource();
    }

    @Override
    public void close() {
        this.container.close();
    }

    @Nonnull
    public DataSource getDataSource() {
        return dataSource;
    }

    @Nonnull
    public String getUrl() {
        return container.getJdbcUrl();
    }

    @Nonnull
    public String getUsername() {
        return container.getUsername();
    }

    @Nonnull
    public String getPassword() {
        return container.getPassword();
    }

    @Nonnull
    private DataSource buildDataSource() {
        final BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(container.getJdbcUrl());
        basicDataSource.setUsername(container.getUsername());
        basicDataSource.setPassword(container.getPassword());
        basicDataSource.setDriverClassName(container.getDriverClassName());
        return basicDataSource;
    }
}
