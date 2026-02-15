/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing database health information")
class DatabaseHealthResponse(
    @field:Schema(
        description = "List of health data entries in format 'check_name:count'"
    )
    val healthData: List<String>
)
