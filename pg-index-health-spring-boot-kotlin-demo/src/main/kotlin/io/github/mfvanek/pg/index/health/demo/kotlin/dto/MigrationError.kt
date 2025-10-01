/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents an error response for migration operations")
class MigrationError(
    @field:Schema(description = "HTTP status code", example = "400")
    val statusCode: Int,

    @field:Schema(description = "Error message describing the issue", example = "Invalid migration script format")
    val message: String
)
