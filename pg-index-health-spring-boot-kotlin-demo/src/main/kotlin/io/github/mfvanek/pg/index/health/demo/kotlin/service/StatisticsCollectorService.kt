/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.index.health.demo.kotlin.config.StatisticsProperties
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.StatisticsResetException
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
 * @property statisticsProperties Configuration properties for statistics operations
 *
 */
@Service
@Transactional(readOnly = true)
@EnableConfigurationProperties(StatisticsProperties::class)
class StatisticsCollectorService(
    private val jdbcTemplate: JdbcTemplate,
    private val databaseManagement: DatabaseManagement,
    private val statisticsProperties: StatisticsProperties
) {

    private val logger = LoggerFactory.getLogger(StatisticsCollectorService::class.java)

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
     * @throws StatisticsResetException if statistics reset fails
     */
    fun resetStatisticsNoWait() {
        if (!databaseManagement.resetStatistics()) {
            throw StatisticsResetException("Could not reset statistics")
        }
    }

    /**
     * Resets statistics and waits for completion.
     *
     * @return the timestamp after reset
     * @throws StatisticsResetException if statistics reset fails
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun resetStatistics(): OffsetDateTime {
        if (databaseManagement.resetStatistics()) {
            waitForStatisticsCollector()
            return getLastStatsResetTimestampInner()
        }
        throw StatisticsResetException("Could not reset statistics")
    }

    /**
     * Waits for the statistics collector to process the data.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Throws(InterruptedException::class)
    private fun waitForStatisticsCollector() {
        jdbcTemplate.execute("vacuum analyze;")
        
        // Poll for vacuum analyze completion by checking if there are no active vacuum operations
        var attempts = 0
        val maxAttempts = statisticsProperties.vacuumResultPollingAttempts
        while (attempts < maxAttempts) {
            val activeVacuums = jdbcTemplate.queryForObject(
                "select count(*) from pg_stat_progress_vacuum where datname = current_database()", 
                Int::class.java
            ) ?: 0
            
            if (activeVacuums == 0) {
                break
            }
            
            TimeUnit.MILLISECONDS.sleep(statisticsProperties.pollingInterval.toMillis())
            attempts++
        }
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
}
