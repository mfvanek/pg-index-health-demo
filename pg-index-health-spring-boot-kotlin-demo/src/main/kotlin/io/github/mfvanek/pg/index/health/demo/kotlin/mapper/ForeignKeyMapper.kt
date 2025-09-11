/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.mapper

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyColumnDto
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyDto
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.springframework.stereotype.Component

@Component
class ForeignKeyMapper {

    fun toForeignKeyDto(foreignKey: ForeignKey): ForeignKeyDto {
        val columns: List<ForeignKeyColumnDto> = foreignKey.columns.map { column ->
            ForeignKeyColumnDto(
                name = column.columnName,
                nullable = column.isNullable
            )
        }
        
        return ForeignKeyDto(
            tableName = foreignKey.tableName,
            constraintName = foreignKey.constraintName,
            columns = columns
        )
    }
}
