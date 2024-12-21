/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring;

import io.github.mfvanek.pg.index.health.demo.without.spring.support.LogsAwareTestBase;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.ConfigurationCollector;
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
            .hasSize(5)
            .allMatch(l -> l.getMessage().startsWith("Parameter with default value"));
    }
}
