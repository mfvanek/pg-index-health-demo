/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.Locale
import kotlin.test.assertNull

class DefaultControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Test
    fun rootPageShouldRedirectToSwaggerUi() {
        val result = webTestClient!!.get()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isFound
            .expectHeader()
            .location("http://localhost:${port}/actuator/swagger-ui")
            .expectBody()
            .returnResult()
            .responseBody
        assertNull(result)
    }
}
