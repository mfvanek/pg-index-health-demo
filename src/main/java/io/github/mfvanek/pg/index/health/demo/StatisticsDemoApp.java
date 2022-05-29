/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.common.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.common.management.DatabaseManagement;
import io.github.mfvanek.pg.common.management.DatabaseManagementImpl;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import javax.annotation.Nonnull;

public class StatisticsDemoApp {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsDemoApp.class);

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
            final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
            final DatabaseManagement databaseManagement = new DatabaseManagementImpl(haPgConnection, new MaintenanceFactoryImpl());
            databaseManagement.resetStatistics();
            waitForStatisticsCollector(embeddedPostgres);
            final Optional<OffsetDateTime> resetTimestamp = databaseManagement.getLastStatsResetTimestamp();
            resetTimestamp.ifPresent(offsetDateTime ->
                    logger.info("Last statistics reset was at {}", offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void waitForStatisticsCollector(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection();
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
