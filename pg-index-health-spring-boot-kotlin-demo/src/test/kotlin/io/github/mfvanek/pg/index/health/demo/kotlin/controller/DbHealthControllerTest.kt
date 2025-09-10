/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.DatabaseHealthResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import kotlin.test.assertTrue

class DbHealthControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Test
    fun shouldCollectHealthData() {
        webTestClient!!.get()
            .uri("/db/health")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(DatabaseHealthResponse::class.java)
            .consumeWith { response ->
                val healthResponse = response.responseBody
                assertTrue(healthResponse != null)
                assertTrue(healthResponse.healthData.isNotEmpty())
            }
    }
}
