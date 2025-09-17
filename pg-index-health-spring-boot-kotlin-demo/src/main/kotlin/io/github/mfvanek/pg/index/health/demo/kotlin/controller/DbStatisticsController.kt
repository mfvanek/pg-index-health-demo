/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.StatisticsResetResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.StatisticsResetException
import io.github.mfvanek.pg.index.health.demo.kotlin.service.StatisticsCollectorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for database statistics operations.
 *
 * @property statisticsCollectorService Service for collecting and resetting database statistics
 */
@RestController
@RequestMapping("/db/statistics")
@Tag(name = "Database Statistics", description = "Endpoints for managing database statistics")
class DbStatisticsController(
    private val statisticsCollectorService: StatisticsCollectorService
) {

    @Operation(
        summary = "Get last statistics reset date",
        description = "Retrieves the timestamp of when database statistics were last reset"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the last reset timestamp",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = StatisticsResetResponse::class)
            )
        ]
    )
    @GetMapping("/reset")
    fun getLastResetDate(): StatisticsResetResponse {
        val timestamp = statisticsCollectorService.getLastStatsResetTimestamp()
        return StatisticsResetResponse(timestamp)
    }

    @Operation(
        summary = "Reset database statistics",
        description = "Resets the database statistics counters. Can optionally wait for completion."
    )
    @Parameter(
        name = "wait",
        description = "Whether to wait for the reset operation to complete before returning",
        required = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "Statistics reset completed successfully with wait",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = StatisticsResetResponse::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "202",
        description = "Statistics reset initiated successfully without wait",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = StatisticsResetResponse::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Statistics reset failed",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = MigrationError::class)
            )
        ]
    )
    @PostMapping("/reset")
    fun doReset(@RequestBody wait: Boolean): ResponseEntity<StatisticsResetResponse> {
        return if (wait) {
            val timestamp = statisticsCollectorService.resetStatistics()
            ResponseEntity.ok().body(StatisticsResetResponse(timestamp))
        } else {
            statisticsCollectorService.resetStatisticsNoWait()
            val timestamp = statisticsCollectorService.getLastStatsResetTimestamp()
            ResponseEntity.accepted().body(StatisticsResetResponse(timestamp))
        }
    }

    /**
     * Handles statistics reset exceptions.
     *
     * @param statisticsResetException exception to handle
     * @return error response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StatisticsResetException::class)
    fun handleStatisticsException(statisticsResetException: StatisticsResetException): MigrationError {
        return MigrationError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Statistics reset failed: ${statisticsResetException.message}"
        )
    }
}
