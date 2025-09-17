/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents a column in a foreign key constraint")
data class ForeignKeyColumnDto(
    @field:Schema(description = "Name of the column", example = "buyer_id")
    val name: String,
    @field:Schema(description = "Indicates whether the column allows NULL values", example = "false")
    val nullable: Boolean
)
