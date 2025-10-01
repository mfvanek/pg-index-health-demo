/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ExtendWith(OutputCaptureExtension::class)
class DbStatisticsControllerMockTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private lateinit var databaseManagement: DatabaseManagement

    @BeforeEach
    fun setUp() {
        `when`(databaseManagement.resetStatistics()).thenReturn(true)
    }

    @Test
    fun shouldThrowExceptionWhenResetStatisticsWithoutWaitFails() {
        `when`(databaseManagement.resetStatistics()).thenReturn(false)

        webTestClient.post()
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
