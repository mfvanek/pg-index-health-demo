/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.MigrationException
import io.github.mfvanek.pg.index.health.demo.kotlin.service.DbMigrationGeneratorService
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean

class DbMigrationControllerMockTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private lateinit var dbMigrationGeneratorService: DbMigrationGeneratorService

    @Test
    fun `returns migration error when keys after are not empty`() {
        val migrationException = MigrationException("There should be no foreign keys not covered by some index")
        Mockito.`when`(dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked())
            .thenThrow(migrationException)

        val result = webTestClient
            .post()
            .uri { uriBuilder ->
                uriBuilder
                    .pathSegment("db", "migration", "generate")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .headers { setUpBasicAuth(it) }
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.EXPECTATION_FAILED)
            .expectBody(MigrationError::class.java)
            .returnResult()
            .responseBody

        assertThat(result)
            .isNotNull
            .isInstanceOf(MigrationError::class.java)
        assertThat(result!!.statusCode)
            .isEqualTo(HttpStatus.EXPECTATION_FAILED.value())
        assertThat(result.message)
            .contains("Migrations failed: There should be no foreign keys not covered by some index")
    }
}
