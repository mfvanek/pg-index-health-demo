/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Service for collecting and resetting database statistics.
 *
 * @property jdbcTemplate JDBC template for executing SQL queries
 * @property databaseManagement Database management instance for statistics operations
 *
 * TODO: move private methods to the bottom of the class
 *
 */
@Service
@Transactional(readOnly = true)
class StatisticsCollectorService(
    private val jdbcTemplate: JdbcTemplate,
    private val databaseManagement: DatabaseManagement
) {

    private val logger = LoggerFactory.getLogger(StatisticsCollectorService::class.java)

    /**
     * Waits for the statistics collector to process the data.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    @Throws(InterruptedException::class)
    private fun waitForStatisticsCollector() {
        jdbcTemplate.execute("vacuum analyze;")
        TimeUnit.MILLISECONDS.sleep(1000L) // TODO: can we wait for a result from vacuum analyze?
    }

    /**
     * Gets the last statistics reset timestamp from the database.
     *
     * @return the last reset timestamp or [OffsetDateTime.MIN] if not available
     */
    private fun getLastStatsResetTimestampInner(): OffsetDateTime {
        val result = databaseManagement.lastStatsResetTimestamp.orElse(OffsetDateTime.MIN)
        logger.trace("Last stats reset timestamp = {}", result)
        return result
    }

    /**
     * Gets the last statistics reset timestamp.
     *
     * @return the last reset timestamp
     */
    fun getLastStatsResetTimestamp(): OffsetDateTime {
        return getLastStatsResetTimestampInner()
    }

    /**
     * Resets statistics without waiting for completion.
     *
     * @return true if reset was successful, false otherwise
     */
    fun resetStatisticsNoWait(): Boolean {
        return databaseManagement.resetStatistics()
    }

    /**
     * Resets statistics and waits for completion.
     *
     * @return the timestamp after reset
     * @throws IllegalStateException if statistics reset fails
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun resetStatistics(): OffsetDateTime {
        if (databaseManagement.resetStatistics()) {
            waitForStatisticsCollector()
            return getLastStatsResetTimestampInner()
        }
        throw IllegalStateException("Could not reset statistics")
    }
}
