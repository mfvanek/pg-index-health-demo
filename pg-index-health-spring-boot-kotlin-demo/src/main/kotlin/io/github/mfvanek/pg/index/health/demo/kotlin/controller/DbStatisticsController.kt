/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import io.github.mfvanek.pg.index.health.demo.kotlin.service.StatisticsCollectorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

/**
 * Controller for database statistics operations.
 *
 * @property statisticsCollectorService Service for collecting and resetting database statistics
 */
@RestController
@RequestMapping("/db/statistics")
class DbStatisticsController(
    private val statisticsCollectorService: StatisticsCollectorService
) {

    /**
     * Gets the last statistics reset date.
     *
     * @return response entity containing the last reset timestamp
     */
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
    @PostMapping("/reset")
    fun doReset(@RequestBody wait: Boolean): ResponseEntity<OffsetDateTime> {
        return if (wait) {
            ResponseEntity.ok().body(statisticsCollectorService.resetStatistics())
        } else {
            if (!statisticsCollectorService.resetStatisticsNoWait()) {
                throw IllegalStateException("Could not reset statistics")
            }
            ResponseEntity.accepted().body(statisticsCollectorService.getLastStatsResetTimestamp())
        }
    }
}
