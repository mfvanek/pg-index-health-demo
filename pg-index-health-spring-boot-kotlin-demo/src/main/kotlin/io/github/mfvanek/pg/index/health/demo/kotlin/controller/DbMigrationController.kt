/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyMigrationResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.MigrationError
import io.github.mfvanek.pg.index.health.demo.kotlin.service.DbMigrationGeneratorService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for database migration operations.
 *
 * @property dbMigrationGeneratorService Service for generating database migrations
 */
@RestController
@RequestMapping("/db/migration")
class DbMigrationController(
    private val dbMigrationGeneratorService: DbMigrationGeneratorService
) {

    /**
     * Generates migrations with foreign keys checked.
     *
     * @return response containing foreign keys and generated migrations
     */
    @PostMapping("/generate")
    fun generateMigrationsWithForeignKeysChecked(): ForeignKeyMigrationResponse {
        return dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked()
    }

    /**
     * Handles migration exceptions.
     *
     * @param illegalStateException exception to handle
     * @return error response
     */
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    @ExceptionHandler(IllegalStateException::class)
    fun handleMigrationException(illegalStateException: IllegalStateException): MigrationError {
        return MigrationError(
            HttpStatus.EXPECTATION_FAILED.value(),
            "Migrations failed: ${illegalStateException.message}"
        )
    }
}
