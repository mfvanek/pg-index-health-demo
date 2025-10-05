/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.utils

import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Clock

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Suppress("UnnecessaryAbstractClass")
abstract class BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @LocalServerPort
    protected var port: Int = 0

    @LocalManagementPort
    protected var actuatorPort: Int = 0

    @Autowired
    protected lateinit var clock: Clock

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var securityProperties: SecurityProperties

    protected fun setUpBasicAuth(httpHeaders: HttpHeaders) {
        httpHeaders.setBasicAuth(securityProperties.user.name, securityProperties.user.password)
    }

    companion object {

        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:18.0")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }

        @JvmStatic
        @BeforeAll
        internal fun setUp() {
            postgres.start()
        }
    }
}
