/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.index.health.demo.DatabaseAwareTestBase;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsCollectorTest extends DatabaseAwareTestBase {

    @Test
    void resetStatisticsShouldWork() {
        final ZonedDateTime beforeTest = ZonedDateTime.now();
        final ZonedDateTime zonedDateTime = StatisticsCollector.resetStatistics(EMBEDDED_POSTGRES.getTestDatabase());
        assertThat(zonedDateTime)
                .isAfter(beforeTest);
    }
}
