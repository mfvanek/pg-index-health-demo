/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyMigrationResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.util.UriBuilder

import org.assertj.core.api.Assertions.assertThat

class DbMigrationControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Test
    fun runsMigrations() {
        val result = webTestClient
            .post()
            .uri { uriBuilder: UriBuilder? ->
                uriBuilder!!
                    .pathSegment("db", "migration", "generate")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .headers { httpHeaders: HttpHeaders? -> this.setUpBasicAuth(httpHeaders!!) }
            .exchange()
            .expectStatus().isOk()
            .expectBody<ForeignKeyMigrationResponse?>(ForeignKeyMigrationResponse::class.java)
            .returnResult()
            .getResponseBody()

        assertThat(result).isNotNull()
        assertThat(result!!.foreignKeysBefore).isNotEmpty()
        assertThat(result!!.foreignKeysAfter).isEmpty()
        assertThat(result!!.generatedMigrations).allMatch { s -> s.contains("create index concurrently if not exists") }
    }

    @Test
    fun returnsNothingWithWrongAuthorization() {
        val result = webTestClient
            .post()
            .uri { uriBuilder: UriBuilder? ->
                uriBuilder!!
                    .pathSegment("db", "migration", "generate")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody(MigrationError::class.java)
            .returnResult()
            .getResponseBody()

        assertThat(result).isNull()
    }
}
