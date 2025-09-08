/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.health.logger.Exclusions
import io.github.mfvanek.pg.health.logger.HealthLogger
import io.github.mfvanek.pg.model.context.PgContext
import io.github.mfvanek.pg.model.units.MemoryUnit
import org.springframework.http.ResponseEntity
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
class DbHealthController(
    private val healthLogger: HealthLogger,
    private val pgContext: PgContext
) {

    /**
     * Collects health data from the database.
     *
     * @return response entity containing health data
     */
    @GetMapping
    fun collectHealthData(): ResponseEntity<Collection<String>> {
        val exclusions = Exclusions.builder()
            .withIndexSizeThreshold(0, MemoryUnit.MB)
            .withTableSizeThreshold(1, MemoryUnit.MB)
            .build()
        return ResponseEntity.ok(healthLogger.logAll(exclusions, pgContext)) // TODO: is it possible to return just DTO?
    }
}
