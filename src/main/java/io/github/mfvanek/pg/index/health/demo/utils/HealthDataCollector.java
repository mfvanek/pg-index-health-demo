/*
 * Copyright (c) 2019-2023. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.common.health.logger.Exclusions;
import io.github.mfvanek.pg.common.health.logger.HealthLogger;
import io.github.mfvanek.pg.common.health.logger.KeyValueFileHealthLogger;
import io.github.mfvanek.pg.common.maintenance.DatabaseChecks;
import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;
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
        final HealthLogger healthLogger = new KeyValueFileHealthLogger(credentials, connectionFactory, DatabaseChecks::new);
        final PgContext context = PgContext.of("demo");
        final List<String> healthData = healthLogger.logAll(exclusions, context);
        healthData.forEach(log::info);
        return healthData;
    }
}
