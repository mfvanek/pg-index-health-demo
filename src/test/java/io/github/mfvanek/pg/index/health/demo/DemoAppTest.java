/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.support.LogsAwareTestBase;
import io.github.mfvanek.pg.index.health.demo.utils.HealthDataCollector;
import io.github.mfvanek.pg.index.health.demo.utils.MigrationRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DemoAppTest extends LogsAwareTestBase {

    @BeforeAll
    static void init() {
        registerLoggerOfType(MigrationRunner.class);
        registerLoggerOfType(HealthDataCollector.class);
    }

    @Test
    void shouldWork() {
        assertThatCode(() -> DemoApp.main(new String[]{}))
                .doesNotThrowAnyException();
        assertThat(getLogs())
                .hasSize(15)
                .filteredOn(l -> l.getLoggerName().contains("MigrationRunner"))
                .hasSize(1)
                .allMatch(l -> l.getMessage().startsWith("Migrations have been successfully executed"));
        assertThat(getLogs())
                .filteredOn(l -> l.getLoggerName().contains("HealthDataCollector"))
                .hasSize(14);
    }
}
