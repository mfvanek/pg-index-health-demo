/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.settings.PgParam;
import io.github.mfvanek.pg.settings.ServerSpecification;
import io.github.mfvanek.pg.settings.maintenance.ConfigurationMaintenanceOnHost;
import io.github.mfvanek.pg.settings.maintenance.ConfigurationMaintenanceOnHostImpl;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.util.Set;
import javax.annotation.Nonnull;

public class ConfigurationDemoApp {

    public static void main(String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            checkConfig(embeddedPostgres);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void checkConfig(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
        final ConfigurationMaintenanceOnHost configurationMaintenance = new ConfigurationMaintenanceOnHostImpl(pgConnection);
        final ServerSpecification serverSpecification = ServerSpecification.builder()
                .withCpuCores(Runtime.getRuntime().availableProcessors())
                .withMemoryAmount(16, MemoryUnit.GB)
                .withSSD()
                .build();
        final Set<PgParam> paramsWithDefaultValues = configurationMaintenance.getParamsWithDefaultValues(serverSpecification);
        paramsWithDefaultValues.forEach(System.out::println);
    }
}
