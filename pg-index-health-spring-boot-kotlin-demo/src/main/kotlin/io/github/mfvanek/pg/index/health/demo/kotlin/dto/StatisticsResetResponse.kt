/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Response containing timestamp information for statistics operations")
class StatisticsResetResponse(
    @field:Schema(
        description = "Timestamp of the statistics reset operation",
        example = "2025-09-11T17:00:00+03:00"
    )
    val resetTimestamp: OffsetDateTime
)
