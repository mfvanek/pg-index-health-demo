/*
 * Copyright (c) 2019-2023. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
import io.github.mfvanek.pg.utils.ClockHolder;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsCollectorTest extends DatabaseAwareTestBase {

    @Test
    void resetStatisticsShouldWork() {
        final Clock clock = ClockHolder.clock();
        final ZonedDateTime beforeTest = ZonedDateTime.now(clock);
        final ZonedDateTime zonedDateTime = StatisticsCollector.resetStatistics(getDataSource(), getUrl());
        assertThat(zonedDateTime)
                .isAfter(beforeTest);
    }
}
