/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyColumnDto
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyDto
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyMigrationResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.github.mfvanek.pg.model.context.PgContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DbMigrationControllerTest : BasePgIndexHealthDemoSpringBootTest() {

    @MockitoBean
    private val foreignKeysNotCoveredWithIndex: DatabaseCheckOnCluster<ForeignKey>? = null

    @BeforeEach
    fun setUp() {
        `when`(foreignKeysNotCoveredWithIndex!!.check(any(PgContext::class.java)))
            .thenReturn(listOf(ForeignKey.ofNotNullColumn("test_table", "fk_col", "col_name")))
            .thenReturn(emptyList())
    }
    
    @Test
    fun shouldGenerateMigrationsWithForeignKeysChecked() {
        val result = webTestClient!!.post()
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
        val result = webTestClient!!.post()
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
        val mockForeignKey = ForeignKey.ofNotNullColumn("demo.test_table", "fk_test_column", "test_column")
        
        `when`(foreignKeysNotCoveredWithIndex!!.check(any(PgContext::class.java)))
            .thenReturn(emptyList())
            .thenReturn(listOf(mockForeignKey))

        val result = webTestClient!!.post()
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
        
        verify(foreignKeysNotCoveredWithIndex, times(2)).check(any(PgContext::class.java))
    }

    @Test
    fun shouldDirectlyTestForeignKeyMigrationResponseDtoWithNonEmptyValues() {
        val foreignKeyDto1 = ForeignKeyDto(
            tableName = "table1",
            constraintName = "fk_col1",
            columns = listOf(ForeignKeyColumnDto(name = "ref_col1", nullable = false))
        )
        val foreignKeyDto2 = ForeignKeyDto(
            tableName = "table2",
            constraintName = "fk_col2",
            columns = listOf(ForeignKeyColumnDto(name = "ref_col2", nullable = false))
        )
        val foreignKeysBefore = listOf(foreignKeyDto1, foreignKeyDto2)
        val foreignKeysAfter = listOf(foreignKeyDto1)
        val generatedMigrations = listOf("CREATE INDEX idx1 ON table1 (col1);", "CREATE INDEX idx2 ON table2 (col2);")

        val response = ForeignKeyMigrationResponse(foreignKeysBefore, foreignKeysAfter, generatedMigrations)

        assertEquals(2, response.foreignKeysBefore.size, "getForeignKeysBefore() should return non-empty list")
        assertEquals(1, response.foreignKeysAfter.size, "getForeignKeysAfter() should return non-empty list")
        assertEquals(2, response.generatedMigrations.size, "getGeneratedMigrations() should return non-empty list")
        
        assertEquals("table1", response.foreignKeysBefore[0].tableName, "getForeignKeysBefore() should return the actual list")
        assertEquals("table2", response.foreignKeysBefore[1].tableName, "getForeignKeysBefore() should return the actual list")
        assertEquals("table1", response.foreignKeysAfter[0].tableName, "getForeignKeysAfter() should return the actual list")
        assertEquals("CREATE INDEX idx1 ON table1 (col1);", response.generatedMigrations[0], "getGeneratedMigrations() should return the actual list")
        assertEquals("CREATE INDEX idx2 ON table2 (col2);", response.generatedMigrations[1], "getGeneratedMigrations() should return the actual list")
    }
}
