/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.common.management.DatabaseManagement;
import io.github.mfvanek.pg.common.management.DatabaseManagementImpl;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.connection.PgHostImpl;
import io.github.mfvanek.pg.settings.maintenance.ConfigurationMaintenanceOnHostImpl;
import io.github.mfvanek.pg.statistics.maintenance.StatisticsMaintenanceOnHostImpl;
import io.github.mfvanek.pg.utils.ClockHolder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

@Slf4j
@UtilityClass
public final class StatisticsCollector {

    @Nonnull
    public static ZonedDateTime resetStatistics(@Nonnull final DataSource dataSource,
                                                @Nonnull final String databaseUrl) {
        final PgConnection pgConnection = PgConnectionImpl.of(dataSource, PgHostImpl.ofUrl(databaseUrl));
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final DatabaseManagement databaseManagement = new DatabaseManagementImpl(haPgConnection, StatisticsMaintenanceOnHostImpl::new, ConfigurationMaintenanceOnHostImpl::new);
        databaseManagement.resetStatistics();
        waitForStatisticsCollector(dataSource);
        final Optional<OffsetDateTime> resetTimestamp = databaseManagement.getLastStatsResetTimestamp();
        final ZonedDateTime zonedDateTime = resetTimestamp
                .orElseThrow(IllegalStateException::new)
                .atZoneSameInstant(ClockHolder.clock().getZone());
        log.info("Last statistics reset was at {}", zonedDateTime);
        return zonedDateTime;
    }

    @SneakyThrows
    static void waitForStatisticsCollector(@Nonnull final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            log.info("Waiting for statistics collector via executing 'vacuum analyze' command");
            statement.execute("vacuum analyze;");
            Thread.sleep(1000L);
        }
    }
}
