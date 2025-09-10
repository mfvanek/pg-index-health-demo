/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin

import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

import org.assertj.core.api.Assertions.assertThat

class ActuatorEndpointTest : BasePgIndexHealthDemoSpringBootTest() {

    private var actuatorClient: WebTestClient? = null

    @BeforeEach
    fun setUp() {
        actuatorClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$actuatorPort/actuator/")
            .defaultHeaders { super.setUpBasicAuth(it) }
            .build()
    }

    @Test
    fun actuatorShouldBeRunOnSeparatePort() {
        assertThat(actuatorPort)
            .isNotEqualTo(port)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "prometheus|jvm_threads_live_threads|text/plain",
            "health|{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}|application/json",
            "health/liveness|{\"status\":\"UP\"}|application/json",
            "health/readiness|{\"status\":\"UP\"}|application/json",
            "liquibase|{\"contexts\":{\"pg-index-health-spring-boot-kotlin-demo\":{\"liquibaseBeans\":{\"liquibase\":{\"changeSets\"|application/json",
            "info|\"version\":|application/json"
        ], delimiter = '|'
    )
    fun actuatorEndpointShouldReturnOk(
        endpointName: String,
        expectedSubstring: String,
        mediaType: String
    ) {
        val result = actuatorClient!!.get()
            .uri { uriBuilder ->
                uriBuilder.path(endpointName).build()
            }
            .accept(MediaType.valueOf(mediaType))
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
        assertThat(result)
            .contains(expectedSubstring)
    }

    @Test
    fun swaggerUiEndpointShouldReturnFound() {
        val result = actuatorClient!!.get()
            .uri { uriBuilder ->
                uriBuilder.pathSegment("swagger-ui").build()
            }
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isFound
            .expectHeader().location("/actuator/swagger-ui/index.html") // TODO: fix swagger for local deployment
            .expectBody()
            .returnResult()
            .responseBody
        assertThat(result).isNull()
    }

    @Test
    fun readinessProbeShouldBeCollectedFromApplicationMainPort() {
        val result = webTestClient!!.get()
            .uri { uriBuilder ->
                uriBuilder.pathSegment("readyz").build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .headers { this.setUpBasicAuth(it) }
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
        assertThat(result)
            .isEqualTo("{\"status\":\"UP\"}")

        val metricsResult = actuatorClient!!.get()
            .uri { uriBuilder ->
                uriBuilder.path("prometheus").build()
            }
            .accept(MediaType.valueOf("text/plain"))
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
        assertThat(metricsResult)
            .contains("http_server_requests_seconds_bucket")
    }
}
