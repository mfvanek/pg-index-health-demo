/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
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
import io.github.mfvanek.pg.settings.maintenance.ConfigurationMaintenanceOnHostImpl;
import io.github.mfvanek.pg.statistics.maintenance.StatisticsMaintenanceOnHostImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

public final class StatisticsCollector {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsCollector.class);

    private StatisticsCollector() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static ZonedDateTime resetStatistics(@Nonnull final DataSource dataSource) {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(dataSource);
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final DatabaseManagement databaseManagement = new DatabaseManagementImpl(haPgConnection, StatisticsMaintenanceOnHostImpl::new, ConfigurationMaintenanceOnHostImpl::new);
        databaseManagement.resetStatistics();
        waitForStatisticsCollector(dataSource);
        final Optional<OffsetDateTime> resetTimestamp = databaseManagement.getLastStatsResetTimestamp();
        final ZonedDateTime zonedDateTime = resetTimestamp
                .orElseThrow(IllegalStateException::new)
                .atZoneSameInstant(ZoneId.systemDefault());
        logger.info("Last statistics reset was at {}", zonedDateTime);
        return zonedDateTime;
    }

    static void waitForStatisticsCollector(@Nonnull final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("vacuum analyze;");
            Thread.sleep(1000L);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
