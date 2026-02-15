/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.StatisticsResetResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.MediaType

@ExtendWith(OutputCaptureExtension::class)
class DbStatisticsControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Test
    fun shouldGetLastResetDate() {
        webTestClient.get()
            .uri("/db/statistics/reset")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(StatisticsResetResponse::class.java)
    }

    @Test
    fun shouldResetStatisticsWithWait() {
        webTestClient.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(true)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(StatisticsResetResponse::class.java)
    }

    @Test
    fun shouldResetStatisticsWithoutWait() {
        webTestClient.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(false)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isAccepted
            .expectBody(StatisticsResetResponse::class.java)
    }
}
