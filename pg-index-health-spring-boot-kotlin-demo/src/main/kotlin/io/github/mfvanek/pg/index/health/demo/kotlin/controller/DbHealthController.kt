/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.health.logger.Exclusions
import io.github.mfvanek.pg.health.logger.HealthLogger
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.DatabaseHealthResponse
import io.github.mfvanek.pg.model.context.PgContext
import io.github.mfvanek.pg.model.units.MemoryUnit
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for database health checks.
 *
 * @property healthLogger Logger for database health checks
 * @property pgContext PostgreSQL context
 */
@RestController
@RequestMapping("/db/health")
@Tag(name = "Database Health", description = "Endpoints for checking database health")
class DbHealthController(
    private val healthLogger: HealthLogger,
    private val pgContext: PgContext
) {

    /**
     * Collects health data from the database.
     *
     * @return database health response DTO
     */
    @Operation(
        summary = "Collect database health data",
        description = "Collects comprehensive health data from the database including information about indexes, tables, and other database objects"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully collected health data",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DatabaseHealthResponse::class)
        )]
    )
    @GetMapping
    fun collectHealthData(): DatabaseHealthResponse {
        val exclusions = Exclusions.builder()
            .withIndexSizeThreshold(0, MemoryUnit.MB)
            .withTableSizeThreshold(1, MemoryUnit.MB)
            .build()
        val healthData = healthLogger.logAll(exclusions, pgContext)
        return DatabaseHealthResponse(healthData.toList())
    }
}
