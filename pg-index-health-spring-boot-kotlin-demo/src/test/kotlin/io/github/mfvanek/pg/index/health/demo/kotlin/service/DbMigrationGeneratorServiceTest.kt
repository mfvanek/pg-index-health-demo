/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyList

@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension::class)
class DbMigrationGeneratorServiceTest : BasePgIndexHealthDemoSpringBootTest() {

    @Autowired
    private lateinit var dbMigrationGeneratorService: DbMigrationGeneratorService

    @MockitoBean
    private lateinit var dbMigrationGenerator: DbMigrationGenerator<ForeignKey>

    @Test
    fun throwsIllegalStateExceptionWhenEmptyMigrationString(capturedOutput: CapturedOutput) {
        val foreignKeys = dbMigrationGeneratorService.getForeignKeysFromDb()
        `when`(dbMigrationGenerator.generate(foreignKeys)).thenReturn(emptyList())

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
        }.apply {
            kotlin.test.assertEquals("There should be no foreign keys not covered by the index", message)
        }
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Generated migrations: []"))
    }

    @Test
    fun logsAboutSqlExceptionWhenBadMigrationStringAndThrowsExceptionAfter(capturedOutput: CapturedOutput) {
        val foreignKeys = dbMigrationGeneratorService.getForeignKeysFromDb()
        `when`(dbMigrationGenerator.generate(foreignKeys)).thenReturn(listOf("select * from payments"))

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
        }.apply {
            kotlin.test.assertEquals("There should be no foreign keys not covered by the index", message)
        }
        
        kotlin.test.assertTrue(capturedOutput.all.contains("Error running migration"))
    }
}
