/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.health.logger.DatabaseChecksOnCluster;
import io.github.mfvanek.pg.health.logger.Exclusions;
import io.github.mfvanek.pg.health.logger.HealthLogger;
import io.github.mfvanek.pg.health.logger.KeyValueFileHealthLogger;
import io.github.mfvanek.pg.model.context.PgContext;
import io.github.mfvanek.pg.model.units.MemoryUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import javax.annotation.Nonnull;

@Slf4j
@UtilityClass
public final class HealthDataCollector {

    @Nonnull
    public static List<String> collectHealthData(@Nonnull final HighAvailabilityPgConnectionFactory connectionFactory,
                                                 @Nonnull final ConnectionCredentials credentials) {
        final Exclusions exclusions = Exclusions.builder()
            .withIndexSizeThreshold(1, MemoryUnit.MB)
            .withTableSizeThreshold(1, MemoryUnit.MB)
            .build();
        final HealthLogger healthLogger = new KeyValueFileHealthLogger(credentials, connectionFactory, DatabaseChecksOnCluster::new);
        final PgContext context = PgContext.of("demo");
        final List<String> healthData = healthLogger.logAll(exclusions, context);
        healthData.forEach(log::info);
        return healthData;
    }
}
