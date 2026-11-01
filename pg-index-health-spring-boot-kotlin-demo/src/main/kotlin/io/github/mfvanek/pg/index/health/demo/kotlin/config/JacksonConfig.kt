/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import io.github.mfvanek.pg.model.jackson3.PgIndexHealthModelModule
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class JacksonConfig {

    @Bean
    fun customizer(): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer { builder ->
        builder.addModule(PgIndexHealthModelModule())
    }
}
