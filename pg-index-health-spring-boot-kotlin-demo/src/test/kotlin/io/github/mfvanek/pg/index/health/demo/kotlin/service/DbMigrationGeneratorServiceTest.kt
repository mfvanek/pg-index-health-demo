/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.MigrationException
import io.github.mfvanek.pg.index.health.demo.kotlin.mapper.ForeignKeyMapper
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.github.mfvanek.pg.model.context.PgContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as mockWhen

@ExtendWith(OutputCaptureExtension::class)
class DbMigrationGeneratorServiceTest : BasePgIndexHealthDemoSpringBootTest() {
    
    private val expectedErrorMessage = "There should be no foreign keys not covered by some index"

    @Autowired
    private var dbMigrationGeneratorService: DbMigrationGeneratorService? = null

    @MockitoBean
    private var dbMigrationGenerator: DbMigrationGenerator<ForeignKey>? = null
    
    @MockitoBean
    private var foreignKeysNotCoveredWithIndex: DatabaseCheckOnCluster<ForeignKey>? = null
    
    @MockitoBean
    private var pgContext: PgContext? = null

    private val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))

    @BeforeEach
    fun setUp() {
        `when`(foreignKeysNotCoveredWithIndex!!.check(pgContext!!)).thenReturn(mockForeignKeys)
    }

    @Test
    fun throwsMigrationExceptionWhenEmptyMigrationString(capturedOutput: CapturedOutput) {
        `when`(dbMigrationGenerator!!.generate(mockForeignKeys)).thenReturn(emptyList())

        assertThrows<MigrationException> {
            dbMigrationGeneratorService!!.generateMigrationsWithForeignKeysChecked()
        }.apply {
            assertEquals(expectedErrorMessage, message)
        }

        assertTrue(capturedOutput.all.contains("Generated migrations: []"))
    }

    @Test
    fun logsAboutSqlExceptionWhenBadMigrationStringAndThrowsExceptionAfter(capturedOutput: CapturedOutput) {
        `when`(dbMigrationGenerator!!.generate(mockForeignKeys)).thenReturn(listOf("select * from payments"))

        assertThrows<MigrationException> {
            dbMigrationGeneratorService!!.generateMigrationsWithForeignKeysChecked()
        }.apply {
            assertEquals(expectedErrorMessage, message)
        }

        assertTrue(capturedOutput.all.contains("Error running migration"))
    }

    @Test
    @Suppress("Unchecked_Cast")
    fun successfullyExecutesMigrationStatementsWithoutException(capturedOutput: CapturedOutput) {
        val mockDataSource = mock(DataSource::class.java)
        val mockConnection = mock(Connection::class.java)
        val mockStatement = mock(Statement::class.java)
        val mockDbMigrationGenerator = mock(DbMigrationGenerator::class.java)
        val mockForeignKeysNotCoveredWithIndex = mock(DatabaseCheckOnCluster::class.java)
        val mockPgContext = mock(PgContext::class.java)
        val mockForeignKeyMapper = mock(ForeignKeyMapper::class.java)
        val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))
        
        val dbMigrationGeneratorServiceWithMocks = DbMigrationGeneratorService(
            mockDataSource,
            mockDbMigrationGenerator as DbMigrationGenerator<ForeignKey>,
            mockForeignKeysNotCoveredWithIndex as DatabaseCheckOnCluster<ForeignKey>,
            mockPgContext,
            mockForeignKeyMapper
        )
        
        `when`(mockForeignKeysNotCoveredWithIndex.check(mockPgContext)).thenReturn(mockForeignKeys).thenReturn(emptyList())
        `when`(mockDbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);"))
        
        `when`(mockDataSource.connection).thenReturn(mockConnection)
        `when`(mockConnection.createStatement()).thenReturn(mockStatement)
        `when`(mockStatement.execute(any(String::class.java))).thenReturn(true)
        
        assertDoesNotThrow {
            dbMigrationGeneratorServiceWithMocks.generateMigrationsWithForeignKeysChecked()
        }
        
        verify(mockStatement).execute("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);")

        assertTrue(capturedOutput.all.contains("Generated migrations: [CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);]"))
    }
    
    @Test
    @Suppress("Unchecked_Cast")
    fun logsErrorWhenCannotGetConnection(capturedOutput: CapturedOutput) {
        val mockDataSource = mock(DataSource::class.java)
        val mockDbMigrationGenerator = mock(DbMigrationGenerator::class.java)
        val mockForeignKeysNotCoveredWithIndex = mock(DatabaseCheckOnCluster::class.java)
        val mockPgContext = mock(PgContext::class.java)
        val mockForeignKeyMapper = mock(ForeignKeyMapper::class.java)
        val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))
        
        val dbMigrationGeneratorServiceWithMockDataSource = DbMigrationGeneratorService(
            mockDataSource,
            mockDbMigrationGenerator as DbMigrationGenerator<ForeignKey>,
            mockForeignKeysNotCoveredWithIndex as DatabaseCheckOnCluster<ForeignKey>,
            mockPgContext,
            mockForeignKeyMapper
        )
        
        `when`(mockForeignKeysNotCoveredWithIndex.check(mockPgContext)).thenReturn(mockForeignKeys)
        `when`(mockDbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);"))
        
        mockWhen(mockDataSource.connection).thenThrow(java.sql.SQLException("Connection failed"))

        assertDoesNotThrow {
            try {
                dbMigrationGeneratorServiceWithMockDataSource.generateMigrationsWithForeignKeysChecked()
            } catch (_: MigrationException) {
            }
        }
        
        assertTrue(capturedOutput.all.contains("Error getting connection"))
    }
}
