/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.checks.custom

import io.github.mfvanek.pg.connection.PgConnection
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.simple.JdbcClient

@TestConfiguration(proxyBeanMethods = false)
class CustomChecksConfig {

    @Bean
    fun allDateTimeColumnsShouldEndWithAtCheckOnHost(
        pgConnection: PgConnection
    ): AllDateTimeColumnsShouldEndWithAtCheckOnHost {
        return AllDateTimeColumnsShouldEndWithAtCheckOnHost(pgConnection)
    }

    @Bean
    fun allPrimaryKeysMustBeNamedAsIdCheckOnHost(
        pgConnection: PgConnection,
        jdbcClient: JdbcClient
    ): AllPrimaryKeysMustBeNamedAsIdCheckOnHost {
        return AllPrimaryKeysMustBeNamedAsIdCheckOnHost(pgConnection, jdbcClient)
    }
}
