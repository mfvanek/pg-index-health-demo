/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.DatabaseHealthResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.service.DatabaseHealthService
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
 * @property databaseHealthService Service for database health operations
 */
@RestController
@RequestMapping("/db/health")
@Tag(name = "Database Health", description = "Endpoints for checking database health")
class DbHealthController(
    private val databaseHealthService: DatabaseHealthService
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
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = DatabaseHealthResponse::class)
            )
        ]
    )
    @GetMapping
    fun collectHealthData(): DatabaseHealthResponse {
        return databaseHealthService.collectHealthData()
    }
}
