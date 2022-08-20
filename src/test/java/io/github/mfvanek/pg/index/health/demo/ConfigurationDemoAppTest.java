/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.support.LogsAwareTestBase;
import io.github.mfvanek.pg.index.health.demo.utils.ConfigurationCollector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConfigurationDemoAppTest extends LogsAwareTestBase {

    @BeforeAll
    static void init() {
        registerLoggerOfType(ConfigurationCollector.class);
    }

    @Test
    void shouldWork() {
        assertThatCode(() -> ConfigurationDemoApp.main(new String[]{}))
                .doesNotThrowAnyException();
        assertThat(getLogs())
                .hasSize(10)
                .allMatch(l -> l.getMessage().startsWith("Parameter with default value"));
    }
}
