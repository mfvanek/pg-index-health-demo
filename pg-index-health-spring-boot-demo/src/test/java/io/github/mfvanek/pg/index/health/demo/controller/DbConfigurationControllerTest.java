/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.settings.PgParam;
import io.github.mfvanek.pg.settings.PgParamImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DbConfigurationControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Test
    void getParamsWithDefaultValuesShouldReturnOk() {
        final PgParam[] result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "configuration")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(PgParam[].class)
            .returnResult()
            .getResponseBody();
        assertThat(result)
            .containsExactly(
                PgParamImpl.of("maintenance_work_mem", "64MB"),
                PgParamImpl.of("random_page_cost", "4"),
                PgParamImpl.of("shared_buffers", "128MB"),
                PgParamImpl.of("lock_timeout", "0"),
                PgParamImpl.of("effective_cache_size", "4GB"),
                PgParamImpl.of("temp_file_limit", "-1"),
                PgParamImpl.of("statement_timeout", "0"),
                PgParamImpl.of("log_min_duration_statement", "-1"),
                PgParamImpl.of("work_mem", "4MB"),
                PgParamImpl.of("idle_in_transaction_session_timeout", "0"));
    }
}
