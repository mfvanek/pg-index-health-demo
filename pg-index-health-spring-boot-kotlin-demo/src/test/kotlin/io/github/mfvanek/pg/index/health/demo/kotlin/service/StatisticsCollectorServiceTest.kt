/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.StatisticsResetException
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
@ExtendWith(OutputCaptureExtension::class)
class StatisticsCollectorServiceTest : BasePgIndexHealthDemoSpringBootTest() {

    @Autowired
    private var statisticsCollectorService: StatisticsCollectorService? = null

    @MockitoBean
    private var databaseManagement: DatabaseManagement? = null

    @MockitoBean
    override var jdbcTemplate: JdbcTemplate? = null

    @Test
    fun getLastStatsResetTimestampShouldReturnCorrectValue() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(expectedTimestamp, result)
    }

    @Test
    fun getLastStatsResetTimestampShouldReturnMinWhenNotAvailable() {
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.empty())

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(OffsetDateTime.MIN, result)
    }

    @Test
    fun getLastStatsResetTimestampShouldReturnCorrectValueAndLogTraceMessage(capturedOutput: CapturedOutput) {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(expectedTimestamp, result)
        
        assertTrue(capturedOutput.all.contains("Last stats reset timestamp = $expectedTimestamp"))
    }

    @Test
    fun resetStatisticsShouldCallWaitForStatisticsCollector() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))
        
        `when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }
        
        statisticsCollectorService!!.resetStatistics()
        
        verify(jdbcTemplate!!).execute("vacuum analyze;")
    }

    @Test
    fun resetStatisticsShouldReturnTimestampWhenSuccessful() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.resetStatistics()
        assertNotNull(result)
        assertEquals(expectedTimestamp, result)
    }

    @Test
    fun resetStatisticsShouldThrowExceptionWhenFailed() {
        `when`(databaseManagement!!.resetStatistics()).thenReturn(false)

        assertThrows<StatisticsResetException> {
            statisticsCollectorService!!.resetStatistics()
        }
    }

    @Test
    fun resetStatisticsShouldCallJdbcTemplateExecute() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        statisticsCollectorService!!.resetStatistics()

        verify(jdbcTemplate!!).execute("vacuum analyze;")
    }

    @Test
    fun resetStatisticsShouldTakeSomeTimeDueToWaitingForVacuumAnalyze() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        `when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }

        // Mock the query that checks for active vacuum operations to return 1 first (active), then 0 (completed)
        `when`(jdbcTemplate!!.queryForObject(
            "select count(*) from pg_stat_progress_vacuum where datname = current_database()", 
            Int::class.java
        )).thenReturn(1).thenReturn(0)

        val startTime = System.currentTimeMillis()
        
        statisticsCollectorService!!.resetStatistics()
        
        val endTime = System.currentTimeMillis()
        
        val duration = endTime - startTime
        assertTrue(duration >= 100) // Should take some time due to polling
    }

    @Test
    fun resetStatisticsShouldHandleMaxAttemptsReached() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        `when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }

        // Mock the query to always return 1 (active vacuum), forcing max attempts to be reached
        `when`(jdbcTemplate!!.queryForObject(
            "select count(*) from pg_stat_progress_vacuum where datname = current_database()", 
            Int::class.java
        )).thenReturn(1)

        statisticsCollectorService!!.resetStatistics()
        
        // Verify the query was called the expected number of times (maxAttempts)
        verify(jdbcTemplate!!, times(10)).queryForObject(
            "select count(*) from pg_stat_progress_vacuum where datname = current_database()", 
            Int::class.java
        )
    }

    @Test
    fun resetStatisticsShouldHandleNullQueryResult() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        `when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        `when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        `when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }

        // Mock the query to return null first, then 0 (completed)
        `when`(jdbcTemplate!!.queryForObject(
            "select count(*) from pg_stat_progress_vacuum where datname = current_database()", 
            Int::class.java
        )).thenReturn(null).thenReturn(0)

        statisticsCollectorService!!.resetStatistics()
    }
}
