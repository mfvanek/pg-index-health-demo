/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-spring-boot-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.common.management.DatabaseManagement;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DbStatisticsControllerMockTest extends BasePgIndexHealthDemoSpringBootTest {

    @MockBean
    private DatabaseManagement databaseManagement;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnErrorWhenResetStatisticsUnsuccessful(final boolean wait) {
        Mockito.when(databaseManagement.resetStatistics())
            .thenReturn(Boolean.FALSE);
        final Object result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "statistics", "reset")
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(wait)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(Object.class)
            .returnResult()
            .getResponseBody();
        assertThat(result)
            .isNotNull()
            .satisfies(b -> assertThat(b.toString()).contains("Internal Server Error"));
    }
}
