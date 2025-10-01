/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "pgih.kotlin.app.statistics")
class StatisticsProperties(
    @DefaultValue("10")
    val vacuumResultPollingAttempts: Int,

    @DefaultValue("100ms")
    val pollingInterval: Duration
)
