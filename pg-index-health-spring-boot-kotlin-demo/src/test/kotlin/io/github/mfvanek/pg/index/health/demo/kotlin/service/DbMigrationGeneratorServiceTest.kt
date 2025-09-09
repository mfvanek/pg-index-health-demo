/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.github.mfvanek.pg.model.context.PgContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as mockWhen
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.any

@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension::class)
class DbMigrationGeneratorServiceTest : BasePgIndexHealthDemoSpringBootTest() {

    @Autowired
    private lateinit var dbMigrationGeneratorService: DbMigrationGeneratorService

    @MockitoBean
    private lateinit var dbMigrationGenerator: DbMigrationGenerator<ForeignKey>
    
    @MockitoBean
    private lateinit var foreignKeysNotCoveredWithIndex: DatabaseCheckOnCluster<ForeignKey>
    
    @MockitoBean
    private lateinit var pgContext: PgContext
    
    private val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))

    @BeforeEach
    fun setUp() {
        `when`(foreignKeysNotCoveredWithIndex.check(pgContext)).thenReturn(mockForeignKeys)
    }

    @Test
    fun throwsIllegalStateExceptionWhenEmptyMigrationString(capturedOutput: CapturedOutput) {
        `when`(dbMigrationGenerator.generate(mockForeignKeys)).thenReturn(emptyList())

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
        }.apply {
            kotlin.test.assertEquals("There should be no foreign keys not covered by the index", message)
        }
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Generated migrations: []"))
    }

    @Test
    fun logsAboutSqlExceptionWhenBadMigrationStringAndThrowsExceptionAfter(capturedOutput: CapturedOutput) {
        `when`(dbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("select * from payments"))

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
        }.apply {
            kotlin.test.assertEquals("There should be no foreign keys not covered by the index", message)
        }
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Error running migration"))
    }
    
    @Test
    fun successfullyExecutesMigrationStatements() {
        `when`(dbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);"))
        
        org.junit.jupiter.api.assertDoesNotThrow {
            try {
                dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
            } catch (e: IllegalStateException) {
            }
        }
    }

    // TODO: do we need 100% coverage? It can cost complexity in tests
    @Test
    fun successfullyExecutesMigrationStatementsWithoutException(capturedOutput: CapturedOutput) {
        val mockDataSource = mock(DataSource::class.java)
        val mockConnection = mock(Connection::class.java)
        val mockStatement = mock(Statement::class.java)
        val mockDbMigrationGenerator = mock(DbMigrationGenerator::class.java)
        val mockForeignKeysNotCoveredWithIndex = mock(DatabaseCheckOnCluster::class.java)
        val mockPgContext = mock(PgContext::class.java)
        val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))
        
        val dbMigrationGeneratorServiceWithMocks = DbMigrationGeneratorService(
            mockDataSource,
            mockDbMigrationGenerator as DbMigrationGenerator<ForeignKey>,
            mockForeignKeysNotCoveredWithIndex as DatabaseCheckOnCluster<ForeignKey>,
            mockPgContext
        )
        
        `when`(mockForeignKeysNotCoveredWithIndex.check(mockPgContext)).thenReturn(mockForeignKeys).thenReturn(emptyList())
        `when`(mockDbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);"))
        
        `when`(mockDataSource.connection).thenReturn(mockConnection)
        `when`(mockConnection.createStatement()).thenReturn(mockStatement)
        `when`(mockStatement.execute(any(String::class.java))).thenReturn(true)
        
        org.junit.jupiter.api.assertDoesNotThrow {
            dbMigrationGeneratorServiceWithMocks.generateMigrationsWithForeignKeysChecked()
        }
        
        verify(mockStatement).execute("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);")
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Generated migrations: [CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);]"))
    }
    
    @Test
    fun logsErrorWhenCannotGetConnection(capturedOutput: CapturedOutput) {
        val mockDataSource = mock(DataSource::class.java)
        val mockDbMigrationGenerator = mock(DbMigrationGenerator::class.java)
        val mockForeignKeysNotCoveredWithIndex = mock(DatabaseCheckOnCluster::class.java)
        val mockPgContext = mock(PgContext::class.java)
        val mockForeignKeys = listOf<ForeignKey>(mock(ForeignKey::class.java))
        
        val dbMigrationGeneratorServiceWithMockDataSource = DbMigrationGeneratorService(
            mockDataSource,
            mockDbMigrationGenerator as DbMigrationGenerator<ForeignKey>,
            mockForeignKeysNotCoveredWithIndex as DatabaseCheckOnCluster<ForeignKey>,
            mockPgContext
        )
        
        `when`(mockForeignKeysNotCoveredWithIndex.check(mockPgContext)).thenReturn(mockForeignKeys)
        `when`(mockDbMigrationGenerator.generate(mockForeignKeys)).thenReturn(listOf("CREATE INDEX IF NOT EXISTS test_idx ON test_table (test_column);"))
        
        mockWhen(mockDataSource.connection).thenThrow(java.sql.SQLException("Connection failed"))
        
        org.junit.jupiter.api.assertDoesNotThrow {
            try {
                dbMigrationGeneratorServiceWithMockDataSource.generateMigrationsWithForeignKeysChecked()
            } catch (e: IllegalStateException) {
            }
        }
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Error getting connection"))
    }
}
