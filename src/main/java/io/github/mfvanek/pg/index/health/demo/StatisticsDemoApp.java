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
import io.github.mfvanek.pg.index.health.demo.utils.HealthDataCollector;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class StatisticsDemoApp {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsDemoApp.class);

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
            final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
            final DatabaseManagement databaseManagement = new DatabaseManagementImpl(haPgConnection, new MaintenanceFactoryImpl());
            databaseManagement.resetStatistics();
            HealthDataCollector.waitForStatisticsCollector(embeddedPostgres.getPostgresDatabase());
            final Optional<OffsetDateTime> resetTimestamp = databaseManagement.getLastStatsResetTimestamp();
            resetTimestamp.ifPresent(offsetDateTime ->
                    logger.info("Last statistics reset was at {}", offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
