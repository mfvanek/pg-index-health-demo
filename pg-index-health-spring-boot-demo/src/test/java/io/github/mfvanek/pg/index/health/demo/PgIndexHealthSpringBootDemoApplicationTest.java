/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgIndexHealthSpringBootDemoApplicationTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context.getEnvironment().containsProperty("spring.datasource.url"))
            .isTrue();
        assertThat(context.getBean("highAvailabilityPgConnection"))
            .isNotNull()
            .isInstanceOf(HighAvailabilityPgConnection.class);
        assertThat(context.getBean("corsConfigurer"))
            .isNotNull()
            .isInstanceOf(WebMvcConfigurer.class);
    }

    @Test
    void jdbcQueryTimeoutFromProperties() {
        final JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        assertThat(jdbcTemplate.getQueryTimeout())
            .isEqualTo(4);
    }

    @Test
    @DisplayName("Throws exception when query exceeds timeout")
    void exceptionWithLongQuery() {
        assertThatThrownBy(() -> jdbcTemplate.execute("select pg_sleep(4.1); select version();"))
            .isInstanceOf(DataAccessResourceFailureException.class)
            .hasMessageContaining("ERROR: canceling statement due to user request");
    }

    @Test
    @DisplayName("Does not throw exception when query does not exceed timeout")
    void noExceptionWithNotLongQuery() {
        assertThatNoException().isThrownBy(() -> jdbcTemplate.execute("select pg_sleep(3.9);"));
    }
}
