/*
 * Copyright (c) 2019-2021. Ivan Vakhrushev and others.
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

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class StatisticsDemoApp {

    public static void main(String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getPostgresDatabase());
            final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
            final DatabaseManagement databaseManagement = new DatabaseManagementImpl(haPgConnection, new MaintenanceFactoryImpl());
            databaseManagement.resetStatistics();
            waitForStatisticsCollector(embeddedPostgres);
            final Optional<OffsetDateTime> resetTimestamp = databaseManagement.getLastStatsResetTimestamp();
            resetTimestamp.ifPresent(offsetDateTime ->
                    System.out.println("Last statistics reset was " + offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void waitForStatisticsCollector(@Nonnull final EmbeddedPostgres embeddedPostgres) {
        try (Connection connection = embeddedPostgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("vacuum analyze;");
            Thread.sleep(1000L);
        } catch (SQLException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
}
