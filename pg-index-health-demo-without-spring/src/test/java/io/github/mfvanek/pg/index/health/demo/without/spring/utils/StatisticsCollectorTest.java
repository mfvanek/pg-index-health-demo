/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.core.utils.ClockHolder;
import io.github.mfvanek.pg.index.health.demo.without.spring.support.DatabaseAwareTestBase;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsCollectorTest extends DatabaseAwareTestBase {

    @Test
    void resetStatisticsShouldWork() {
        final Clock clock = ClockHolder.clock();
        final long beforeTest = System.nanoTime();
        final ZonedDateTime zonedDateTime = StatisticsCollector.resetStatistics(getDataSource(), getUrl());
        assertThat(System.nanoTime() - beforeTest)
            .as("Execution time should be greater than 1 second due to delay")
            .isGreaterThanOrEqualTo(1_000_000_000L);
        assertThat(zonedDateTime)
            .isNotNull()
            .isBefore(ZonedDateTime.now(clock));
    }
}
