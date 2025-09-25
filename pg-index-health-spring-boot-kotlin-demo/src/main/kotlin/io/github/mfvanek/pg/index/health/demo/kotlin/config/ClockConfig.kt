/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/**
 * Configuration class for providing a [Clock] bean.
 *
 * This configuration provides a UTC clock that can be injected into tests
 * that need to work with time-related operations. Having a Clock bean makes
 * it easier to test time-dependent code by allowing mocks to be injected.
 *
 * Usage in tests:
 * ```
 * @Autowired
 * private lateinit var clock: Clock
 * ```
 *
 * @see Clock
 * @see java.time.Instant
 */
@Configuration
class ClockConfig {

    /**
     * Provides a system clock in the UTC time zone.
     *
     * @return a [Clock] instance fixed to UTC time zone
     */
    @Bean
    fun clock(): Clock {
        return Clock.systemUTC()
    }
}
