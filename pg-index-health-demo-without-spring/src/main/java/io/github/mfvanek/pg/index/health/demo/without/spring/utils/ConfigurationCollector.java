/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.connection.host.PgHostImpl;
import io.github.mfvanek.pg.core.settings.ConfigurationMaintenanceOnHostImpl;
import io.github.mfvanek.pg.core.statistics.StatisticsMaintenanceOnHostImpl;
import io.github.mfvanek.pg.health.checks.management.DatabaseManagement;
import io.github.mfvanek.pg.health.checks.management.DatabaseManagementImpl;
import io.github.mfvanek.pg.model.settings.PgParam;
import io.github.mfvanek.pg.model.settings.ServerSpecification;
import io.github.mfvanek.pg.model.units.MemoryUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

@Slf4j
@UtilityClass
public class ConfigurationCollector {

    @Nonnull
    public static Set<PgParam> checkConfig(@Nonnull final DataSource dataSource,
                                           @Nonnull final String databaseUrl) {
        final PgConnection pgConnection = PgConnectionImpl.of(dataSource, PgHostImpl.ofUrl(databaseUrl));
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final DatabaseManagement databaseManagement = new DatabaseManagementImpl(
            haPgConnection, StatisticsMaintenanceOnHostImpl::new, ConfigurationMaintenanceOnHostImpl::new);
        final ServerSpecification serverSpecification = ServerSpecification.builder()
            .withCpuCores(Runtime.getRuntime().availableProcessors())
            .withMemoryAmount(16, MemoryUnit.GB)
            .withSSD()
            .build();
        final Set<PgParam> paramsWithDefaultValues = databaseManagement.getParamsWithDefaultValues(serverSpecification);
        paramsWithDefaultValues.forEach(p -> log.info("Parameter with default value {}", p));
        return paramsWithDefaultValues;
    }
}
