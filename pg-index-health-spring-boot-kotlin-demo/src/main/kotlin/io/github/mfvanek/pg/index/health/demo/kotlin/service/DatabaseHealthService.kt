/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.health.logger.Exclusions
import io.github.mfvanek.pg.health.logger.HealthLogger
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.DatabaseHealthResponse
import io.github.mfvanek.pg.model.context.PgContext
import io.github.mfvanek.pg.model.units.MemoryUnit
import org.springframework.stereotype.Service

/**
 * Service for database health operations.
 *
 * @property healthLogger Logger for database health checks
 * @property pgContext PostgreSQL context
 */
@Service
class DatabaseHealthService(
    private val healthLogger: HealthLogger,
    private val pgContext: PgContext
) {

    /**
     * Collects health data from the database.
     *
     * @return database health response DTO
     */
    fun collectHealthData(): DatabaseHealthResponse {
        val exclusions = Exclusions.builder()
            .withIndexSizeThreshold(0, MemoryUnit.MB)
            .withTableSizeThreshold(1, MemoryUnit.MB)
            .build()
        val healthData = healthLogger.logAll(exclusions, pgContext)
        return DatabaseHealthResponse(healthData.toList())
    }
}
