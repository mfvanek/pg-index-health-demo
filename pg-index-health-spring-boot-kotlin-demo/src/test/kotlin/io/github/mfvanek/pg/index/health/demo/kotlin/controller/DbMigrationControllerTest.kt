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
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.model.context.PgContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.test.context.bean.override.mockito.MockitoBean

class DbMigrationControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private lateinit var foreignKeysNotCoveredWithIndex: DatabaseCheckOnCluster<ForeignKey>


    @BeforeEach
    fun setUp() {
        `when`(foreignKeysNotCoveredWithIndex.check(any(PgContext::class.java)))
            .thenReturn(listOf(ForeignKey.ofNotNullColumn("test_table", "fk_col", "col_name"))) // First call returns non-empty list
            .thenReturn(emptyList()) // Second call returns empty list
    }

    
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

        assertEquals(1, result.foreignKeysBefore.size, "getForeignKeysBefore() should return non-empty list")
        assertEquals(0, result.foreignKeysAfter.size, "getForeignKeysAfter() should return empty list")
        assertTrue(result.generatedMigrations.isNotEmpty(), "getGeneratedMigrations() should return non-empty list")
        
        assertEquals("test_table", result.foreignKeysBefore[0].tableName, "getForeignKeysBefore() should return the actual list")
        assertTrue(result.generatedMigrations.all { it.contains("create index concurrently if not exists") }, "getGeneratedMigrations() should return the actual list")
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

        assertEquals(result, null)
    }

    @Test
    fun handleMigrationExceptionShouldReturnExpectedError() {
        // Create a mock foreign key to return when checking for foreign keys after migrations
        val mockForeignKey = ForeignKey.ofNotNullColumn("demo.test_table", "fk_test_column", "test_column")
        
        // Configure the mock to return a non-empty list when called the second time
        // First call (before migrations) returns an empty list
        `when`(foreignKeysNotCoveredWithIndex.check(any(PgContext::class.java)))
            .thenReturn(emptyList()) // Before migrations
            .thenReturn(listOf(mockForeignKey)) // After migrations (simulating failed migrations)

        val result = webTestClient.post()
            .uri("/db/migration/generate")
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.EXPECTATION_FAILED)
            .expectBody(MigrationError::class.java)
            .returnResult()
            .responseBody

        assertTrue(result != null)
        assertEquals(result.statusCode, HttpStatus.EXPECTATION_FAILED.value())
        assertEquals(result.message.contains("Migrations failed:"), true)
        
        // Verify that the check method was called twice (before and after migrations)
        verify(foreignKeysNotCoveredWithIndex, times(2)).check(any(PgContext::class.java))
    }
}
