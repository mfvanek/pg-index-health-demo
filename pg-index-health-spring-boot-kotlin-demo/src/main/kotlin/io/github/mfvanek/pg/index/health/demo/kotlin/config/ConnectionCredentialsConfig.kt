/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ConnectionCredentialsConfig(
    @param:Value("\${spring.datasource.url}") val url: String,
    @param:Value("\${spring.datasource.username}") val username: String,
    @param:Value("\${spring.datasource.password}") val password: String
)
