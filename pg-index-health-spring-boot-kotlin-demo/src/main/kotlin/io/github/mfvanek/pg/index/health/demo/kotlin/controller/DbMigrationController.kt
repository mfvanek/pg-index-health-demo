/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyMigrationResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.MigrationException
import io.github.mfvanek.pg.index.health.demo.kotlin.service.DbMigrationGeneratorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Controller for database migration operations.
 *
 * @property dbMigrationGeneratorService Service for generating database migrations
 */
@RestController
@RequestMapping("/db/migration")
@Tag(name = "Database Migration", description = "Endpoints for generating database migrations")
class DbMigrationController(
    private val dbMigrationGeneratorService: DbMigrationGeneratorService
) {

    /**
     * Generates migrations with foreign keys checked.
     *
     * @return response containing foreign keys and generated migrations
     */
    @Operation(
        summary = "Generate database migrations",
        description = "Generates database migrations with foreign key constraints checked"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully generated migrations",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = ForeignKeyMigrationResponse::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "417",
        description = "Migration generation failed",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = MigrationError::class)
            )
        ]
    )
    @PostMapping("/generate")
    fun generateMigrationsWithForeignKeysChecked(): ForeignKeyMigrationResponse {
        return dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
    }

    /**
     * Handles migration exceptions.
     *
     * @param migrationException exception to handle
     * @return error response
     */
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    @ExceptionHandler(MigrationException::class)
    fun handleMigrationException(migrationException: MigrationException): MigrationError {
        return MigrationError(
            HttpStatus.EXPECTATION_FAILED.value(),
            "Migrations failed: ${migrationException.message}"
        )
    }
}
