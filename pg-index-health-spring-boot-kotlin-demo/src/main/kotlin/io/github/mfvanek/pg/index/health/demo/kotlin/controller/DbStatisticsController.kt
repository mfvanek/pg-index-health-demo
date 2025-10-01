/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.service.StatisticsCollectorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

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

    /**
     * Gets the last statistics reset date.
     *
     * @return response entity containing the last reset timestamp
     */
    @Operation(
        summary = "Get last statistics reset date",
        description = "Retrieves the timestamp of when database statistics were last reset"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the last reset timestamp",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = OffsetDateTime::class)
        )]
    )
    @GetMapping("/reset")
    fun getLastResetDate(): ResponseEntity<OffsetDateTime> {
        return ResponseEntity.ok(statisticsCollectorService.getLastStatsResetTimestamp())
    }

    /**
     * Resets statistics with optional wait.
     *
     * @param wait whether to wait for completion
     * @return response entity containing the timestamp after reset
     */
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
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = OffsetDateTime::class)
        )]
    )
    @ApiResponse(
        responseCode = "202",
        description = "Statistics reset initiated successfully without wait",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = OffsetDateTime::class)
        )]
    )
    @ApiResponse(
        responseCode = "500",
        description = "Statistics reset failed",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = MigrationError::class)
        )]
    )
    @PostMapping("/reset")
    fun doReset(@RequestBody wait: Boolean): ResponseEntity<OffsetDateTime> { // TODO: return DTO
        return if (wait) {
            ResponseEntity.ok().body(statisticsCollectorService.resetStatistics())
        } else {
            if (!statisticsCollectorService.resetStatisticsNoWait()) {
                throw IllegalStateException("Could not reset statistics")
            }
            ResponseEntity.accepted().body(statisticsCollectorService.getLastStatsResetTimestamp())
        }
    }

    /**
     * Handles statistics reset exceptions.
     *
     * @param illegalStateException exception to handle
     * @return error response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalStateException::class)
    fun handleStatisticsException(illegalStateException: IllegalStateException): MigrationError {
        return MigrationError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Statistics reset failed: ${illegalStateException.message}"
        )
    }
}
