/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
import io.github.mfvanek.pg.settings.PgParamImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationCollectorTest extends DatabaseAwareTestBase {

    @Test
    void checkConfigShouldWork() {
        assertThat(ConfigurationCollector.checkConfig(getDataSource(), getUrl()))
            .hasSize(5)
            .containsExactly(
                PgParamImpl.of("effective_cache_size", "4GB"),
                PgParamImpl.of("temp_file_limit", "-1"),
                PgParamImpl.of("statement_timeout", "0"),
                PgParamImpl.of("log_min_duration_statement", "-1"),
                PgParamImpl.of("idle_in_transaction_session_timeout", "0"));
    }
}
