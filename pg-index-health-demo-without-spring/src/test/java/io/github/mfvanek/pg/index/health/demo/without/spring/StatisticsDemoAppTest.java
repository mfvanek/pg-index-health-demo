/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring;

import io.github.mfvanek.pg.index.health.demo.without.spring.support.LogsAwareTestBase;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.StatisticsCollector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class StatisticsDemoAppTest extends LogsAwareTestBase {

    @BeforeAll
    static void init() {
        registerLoggerOfType(StatisticsCollector.class);
    }

    @Test
    void shouldWork() {
        assertThatCode(() -> StatisticsDemoApp.main(new String[]{}))
            .doesNotThrowAnyException();
        assertThat(getLogs())
            .hasSize(2)
            .anyMatch(l -> l.getMessage().startsWith("Last statistics reset was at"))
            .anyMatch(l -> l.getMessage().startsWith("Waiting for statistics collector"));
    }
}
