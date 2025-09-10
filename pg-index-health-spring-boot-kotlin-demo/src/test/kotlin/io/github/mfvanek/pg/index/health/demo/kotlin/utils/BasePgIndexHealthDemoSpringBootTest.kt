/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Clock

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    protected var jdbcTemplate: JdbcTemplate? = null

    @LocalServerPort
    protected var port: Int = 8080

    @LocalManagementPort
    protected var actuatorPort: Int = 8090

    @Autowired
    protected var clock: Clock? = null

    @Autowired
    protected var webTestClient: WebTestClient? = null

    @Autowired
    private var securityProperties: SecurityProperties? = null

    protected fun setUpBasicAuth(httpHeaders: HttpHeaders) {
        httpHeaders.setBasicAuth(securityProperties!!.user.name, securityProperties!!.user.password)
    }
}
