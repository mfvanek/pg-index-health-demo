/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.StatisticsResetResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean

@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension::class)
class DbStatisticsControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private var databaseManagement: DatabaseManagement? = null

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)
    }
    
    @Test
    fun shouldGetLastResetDate() {
        webTestClient!!.get()
            .uri("/db/statistics/reset")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(StatisticsResetResponse::class.java)
    }

    @Test
    fun shouldResetStatisticsWithWait() {
        webTestClient!!.post()
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
        webTestClient!!.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(false)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isAccepted
            .expectBody(StatisticsResetResponse::class.java)
    }

    @Test
    fun shouldThrowExceptionWhenResetStatisticsWithoutWaitFails() {
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(false)

        webTestClient!!.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(false)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody()
            .jsonPath("$.message").isEqualTo("Statistics reset failed: Could not reset statistics")
    }
}
