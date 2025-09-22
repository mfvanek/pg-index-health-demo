/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:postgresql://test-host:5432/test-db",
        "spring.datasource.username=test-user",
        "spring.datasource.password=test-password"
    ]
)
@SpringBootTest(classes = [ConnectionCredentialsConfig::class])
class ConnectionCredentialsConfigTest {

    @Autowired
    private lateinit var connectionCredentialsConfig: ConnectionCredentialsConfig

    @Test
    fun `should bind datasource properties correctly`() {
        assertThat(connectionCredentialsConfig.url).isEqualTo("jdbc:postgresql://test-host:5432/test-db")
        assertThat(connectionCredentialsConfig.username).isEqualTo("test-user")
        assertThat(connectionCredentialsConfig.password).isEqualTo("test-password")
    }
}
