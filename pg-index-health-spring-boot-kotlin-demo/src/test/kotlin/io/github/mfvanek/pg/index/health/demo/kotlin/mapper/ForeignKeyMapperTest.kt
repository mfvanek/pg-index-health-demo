/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.mapper

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyColumnDto
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyDto
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.column.Column
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ForeignKeyMapperTest : BasePgIndexHealthDemoSpringBootTest() {

    @Autowired
    private val mapper: ForeignKeyMapper? = null

    @Test
    fun shouldMapForeignKeyToForeignKeyDto() {
        // Create a ForeignKey with a single non-nullable column
        val column = Column.ofNotNull("users", "user_id")
        val foreignKey = ForeignKey.of("users", "fk_users_roles", listOf(column))

        val dto = mapper!!.toForeignKeyDto(foreignKey)

        assertNotNull(dto)
        assertEquals("users", dto.tableName)
        assertEquals("fk_users_roles", dto.constraintName)
        assertEquals(1, dto.columns.size)
        
        val columnDto = dto.columns[0]
        assertEquals("user_id", columnDto.name)
        assertEquals(false, columnDto.nullable)
    }

    @Test
    fun shouldMapForeignKeyWithMultipleColumns() {
        // Create a ForeignKey with multiple columns
        val column1 = Column.ofNotNull("users", "user_id")
        val column2 = Column.ofNullable("users", "role_id")
        val foreignKey = ForeignKey.of("users", "fk_users_roles", listOf(column1, column2))

        val dto = mapper!!.toForeignKeyDto(foreignKey)

        assertNotNull(dto)
        assertEquals("users", dto.tableName)
        assertEquals("fk_users_roles", dto.constraintName)
        assertEquals(2, dto.columns.size)
        
        val firstColumnDto = dto.columns[0]
        assertEquals("user_id", firstColumnDto.name)
        assertEquals(false, firstColumnDto.nullable)
        
        val secondColumnDto = dto.columns[1]
        assertEquals("role_id", secondColumnDto.name)
        assertEquals(true, secondColumnDto.nullable)
    }

    @Test
    fun shouldMapForeignKeyWithSingleNullableColumn() {
        // Create a ForeignKey with a single nullable column
        val column = Column.ofNullable("users", "user_id")
        val foreignKey = ForeignKey.of("users", "fk_users_roles", listOf(column))

        val dto = mapper!!.toForeignKeyDto(foreignKey)

        assertNotNull(dto)
        assertEquals("users", dto.tableName)
        assertEquals("fk_users_roles", dto.constraintName)
        assertEquals(1, dto.columns.size)
        
        val columnDto = dto.columns[0]
        assertEquals("user_id", columnDto.name)
        assertEquals(true, columnDto.nullable)
    }
}
