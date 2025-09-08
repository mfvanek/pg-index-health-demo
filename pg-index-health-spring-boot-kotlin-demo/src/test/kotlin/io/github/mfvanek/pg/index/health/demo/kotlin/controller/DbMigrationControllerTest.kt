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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.assertTrue

class DbMigrationControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Test
    fun shouldGenerateMigrationsWithForeignKeysChecked() {
        val result = webTestClient.post()
            .uri("/db/migration/generate")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk
            .expectBody(ForeignKeyMigrationResponse::class.java)
            .returnResult()
            .responseBody

        assertTrue(result != null)
        assertTrue(result.foreignKeysBefore.isNotEmpty())
        assertTrue(result.foreignKeysAfter.isEmpty())
        assertTrue(result.generatedMigrations.all { it.contains("create index concurrently if not exists") })
    }

    @Test
    fun returnsNothingWithWrongAuthorization() {
        val result = webTestClient.post()
            .uri("/db/migration/generate")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody(MigrationError::class.java)
            .returnResult()
            .responseBody

        assertTrue(result == null)
    }
}
