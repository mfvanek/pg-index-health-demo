/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents a foreign key constraint in the database")
data class ForeignKeyDto(
    @field:Schema(description = "Name of the table containing the foreign key", example = "orders")
    val tableName: String,
    @field:Schema(description = "Name of the foreign key constraint", example = "fk_orders_buyer_id")
    val constraintName: String,
    @field:Schema(description = "Columns involved in the foreign key", example = "[{\"name\": \"buyer_id\", \"nullable\": false}]")
    val columns: List<ForeignKeyColumnDto>
)
