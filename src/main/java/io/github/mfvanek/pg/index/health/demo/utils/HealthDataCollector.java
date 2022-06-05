/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.common.health.DatabaseHealthFactory;
import io.github.mfvanek.pg.common.health.DatabaseHealthFactoryImpl;
import io.github.mfvanek.pg.common.health.logger.Exclusions;
import io.github.mfvanek.pg.common.health.logger.HealthLogger;
import io.github.mfvanek.pg.common.health.logger.KeyValueFileHealthLogger;
import io.github.mfvanek.pg.common.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PrimaryHostDeterminerImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.annotation.Nonnull;

public final class HealthDataCollector {

    private static final Logger logger = LoggerFactory.getLogger(HealthDataCollector.class);

    private HealthDataCollector() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static List<String> collectHealthData(@Nonnull final String databaseName, final int port) {
        final String url = String.format("jdbc:postgresql://localhost:%d/%s", port, databaseName);
        final ConnectionCredentials credentials = ConnectionCredentials.ofUrl(url, "postgres", "postgres");
        final HighAvailabilityPgConnectionFactory connectionFactory = new HighAvailabilityPgConnectionFactoryImpl(
                new PgConnectionFactoryImpl(), new PrimaryHostDeterminerImpl());
        final DatabaseHealthFactory databaseHealthFactory = new DatabaseHealthFactoryImpl(new MaintenanceFactoryImpl());
        final Exclusions exclusions = Exclusions.builder()
                .withIndexSizeThreshold(1, MemoryUnit.MB)
                .withTableSizeThreshold(1, MemoryUnit.MB)
                .build();
        final HealthLogger healthLogger = new KeyValueFileHealthLogger(credentials, connectionFactory, databaseHealthFactory);
        final PgContext context = PgContext.of("demo");
        final List<String> healthData = healthLogger.logAll(exclusions, context);
        healthData.forEach(logger::info);
        return healthData;
    }
}
