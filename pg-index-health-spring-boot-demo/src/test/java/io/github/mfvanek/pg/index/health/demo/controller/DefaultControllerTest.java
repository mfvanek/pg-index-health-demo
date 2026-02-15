/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Test
    void rootPageShouldRedirectToSwaggerUi() {
        final byte[] result = webTestClient.get()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location(String.format(Locale.ROOT, "http://localhost:%d/actuator/swagger-ui", port))
            .expectBody()
            .returnResult()
            .getResponseBody();
        assertThat(result)
            .isNull();
    }
}
