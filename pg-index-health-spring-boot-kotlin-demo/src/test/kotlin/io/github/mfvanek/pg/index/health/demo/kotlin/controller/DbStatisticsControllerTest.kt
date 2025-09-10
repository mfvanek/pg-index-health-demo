/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.service.StatisticsCollectorService
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.OffsetDateTime

@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension::class)
class DbStatisticsControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private var statisticsCollectorService: StatisticsCollectorService? = null

    @Test
    fun shouldGetLastResetDate() {
        webTestClient!!.get()
            .uri("/db/statistics/reset")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(OffsetDateTime::class.java)
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
            .expectBody(OffsetDateTime::class.java)
    }

    @Test
    fun shouldResetStatisticsWithoutWait() {
        org.mockito.Mockito.`when`(statisticsCollectorService!!.resetStatisticsNoWait()).thenReturn(true)
        
        webTestClient!!.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(false)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isAccepted
            .expectBody(OffsetDateTime::class.java)
    }

    @Test
    fun shouldThrowExceptionWhenResetStatisticsWithoutWaitFails(capturedOutput: CapturedOutput) {
        org.mockito.Mockito.`when`(statisticsCollectorService!!.resetStatisticsNoWait()).thenReturn(false)

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
