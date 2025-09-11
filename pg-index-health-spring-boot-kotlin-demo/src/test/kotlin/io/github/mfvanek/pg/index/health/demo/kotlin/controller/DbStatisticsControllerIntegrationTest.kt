/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.StatisticsResetResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Clock
import java.time.OffsetDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Testcontainers
class DbStatisticsControllerIntegrationTest : BasePgIndexHealthDemoSpringBootTest() {

    companion object {
        @Container
        private val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:17.4")
            .apply {
                withDatabaseName("demo_for_pg_index_health")
                withUsername("demo_user")
                withPassword("myUniquePassword")
            }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresqlContainer::getUsername)
            registry.add("spring.datasource.password", postgresqlContainer::getPassword)
        }
    }

    @Test
    fun shouldResetStatistics() {
        val wait = true

        webTestClient!!.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(wait)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(StatisticsResetResponse::class.java)
            .value { response ->
                validateResetTimestamp(response)
            }
    }

    @Test
    fun shouldResetStatisticsWithoutWait() {
        val wait = false

        webTestClient!!.post()
            .uri("/db/statistics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(wait)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isAccepted
            .expectBody(StatisticsResetResponse::class.java)
            .value { response ->
                validateResetTimestamp(response)
            }
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
            .value { response ->
                validateResetTimestamp(response)
            }
    }
    
    private fun validateResetTimestamp(response: StatisticsResetResponse) {
        assertNotNull(response.resetTimestamp)
        // Verify that the timestamp is recent (within the last minute)
        val now = OffsetDateTime.now(clock!!.zone).withOffsetSameInstant(response.resetTimestamp.offset)
        assertTrue(response.resetTimestamp.isBefore(now.plusSeconds(1)))
        assertTrue(response.resetTimestamp.isAfter(now.minusMinutes(1)))
    }
}
