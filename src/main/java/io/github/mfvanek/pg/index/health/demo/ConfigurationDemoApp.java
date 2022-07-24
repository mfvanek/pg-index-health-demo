/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.common.management.DatabaseManagement;
import io.github.mfvanek.pg.common.management.DatabaseManagementImpl;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.settings.PgParam;
import io.github.mfvanek.pg.settings.ServerSpecification;
import io.github.mfvanek.pg.settings.maintenance.ConfigurationMaintenanceOnHostImpl;
import io.github.mfvanek.pg.statistics.maintenance.StatisticsMaintenanceOnHostImpl;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Nonnull;

public class ConfigurationDemoApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDemoApp.class);

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            checkConfig(embeddedPostgres);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void checkConfig(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final DatabaseManagement databaseManagement = new DatabaseManagementImpl(
                haPgConnection, StatisticsMaintenanceOnHostImpl::new, ConfigurationMaintenanceOnHostImpl::new);
        final ServerSpecification serverSpecification = ServerSpecification.builder()
                .withCpuCores(Runtime.getRuntime().availableProcessors())
                .withMemoryAmount(16, MemoryUnit.GB)
                .withSSD()
                .build();
        final Set<PgParam> paramsWithDefaultValues = databaseManagement.getParamsWithDefaultValues(serverSpecification);
        paramsWithDefaultValues.forEach(p -> LOGGER.info("Parameter with default value {}", p));
    }
}
