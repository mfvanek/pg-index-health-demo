/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.service;

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StatisticsCollectorService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseManagement databaseManagement;

    @SneakyThrows
    private void waitForStatisticsCollector() {
        jdbcTemplate.execute("vacuum analyze;");
        TimeUnit.MILLISECONDS.sleep(1_000L);
    }

    @Nonnull
    private OffsetDateTime getLastStatsResetTimestampInner() {
        final OffsetDateTime result = databaseManagement.getLastStatsResetTimestamp().orElse(OffsetDateTime.MIN);
        log.trace("Last stats reset timestamp = {}", result);
        return result;
    }

    @Nonnull
    public OffsetDateTime getLastStatsResetTimestamp() {
        return getLastStatsResetTimestampInner();
    }

    public boolean resetStatisticsNoWait() {
        return databaseManagement.resetStatistics();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OffsetDateTime resetStatistics() {
        if (databaseManagement.resetStatistics()) {
            waitForStatisticsCollector();
            return getLastStatsResetTimestampInner();
        }
        throw new IllegalStateException("Could not reset statistics");
    }
}
