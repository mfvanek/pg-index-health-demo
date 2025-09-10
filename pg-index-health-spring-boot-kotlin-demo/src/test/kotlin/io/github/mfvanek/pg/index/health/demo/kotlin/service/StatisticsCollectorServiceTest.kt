/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension::class)
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
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(expectedTimestamp, result)
    }

    @Test
    fun getLastStatsResetTimestampShouldReturnMinWhenNotAvailable() {
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.empty())

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(OffsetDateTime.MIN, result)
    }

    @Test
    fun getLastStatsResetTimestampShouldReturnCorrectValueAndLogTraceMessage(capturedOutput: CapturedOutput) {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.getLastStatsResetTimestamp()
        assertEquals(expectedTimestamp, result)
        
        assertTrue(capturedOutput.all.contains("Last stats reset timestamp = $expectedTimestamp"))
    }

    @Test
    fun resetStatisticsShouldCallWaitForStatisticsCollector() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))
        
        Mockito.`when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }
        
        statisticsCollectorService!!.resetStatistics()
        
        Mockito.verify(jdbcTemplate!!).execute("vacuum analyze;")
    }

    @Test
    fun resetStatisticsNoWaitShouldReturnTrueWhenSuccessful() {
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)

        val result = statisticsCollectorService!!.resetStatisticsNoWait()
        assertTrue(result)
    }

    @Test
    fun resetStatisticsNoWaitShouldReturnFalseWhenFailed() {
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(false)

        val result = statisticsCollectorService!!.resetStatisticsNoWait()
        assertFalse(result)
    }

    @Test
    fun resetStatisticsShouldReturnTimestampWhenSuccessful() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        val result = statisticsCollectorService!!.resetStatistics()
        assertNotNull(result)
        assertEquals(expectedTimestamp, result)
    }

    @Test
    fun resetStatisticsShouldThrowExceptionWhenFailed() {
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(false)

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            statisticsCollectorService!!.resetStatistics()
        }
    }

    @Test
    fun resetStatisticsShouldCallJdbcTemplateExecute() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        statisticsCollectorService!!.resetStatistics()

        Mockito.verify(jdbcTemplate!!).execute("vacuum analyze;")
    }

    @Test
    fun resetStatisticsShouldTakeApproximatelyOneSecond() {
        val expectedTimestamp = OffsetDateTime.now(clock!!.zone)
        org.mockito.Mockito.`when`(databaseManagement!!.resetStatistics()).thenReturn(true)
        org.mockito.Mockito.`when`(databaseManagement!!.lastStatsResetTimestamp)
            .thenReturn(java.util.Optional.of(expectedTimestamp))

        Mockito.`when`(jdbcTemplate!!.execute("vacuum analyze;")).thenAnswer { _ -> }

        val startTime = System.currentTimeMillis()
        
        statisticsCollectorService!!.resetStatistics()
        
        val endTime = System.currentTimeMillis()
        
        val duration = endTime - startTime
        assertTrue(duration >= 900)
    }
}
