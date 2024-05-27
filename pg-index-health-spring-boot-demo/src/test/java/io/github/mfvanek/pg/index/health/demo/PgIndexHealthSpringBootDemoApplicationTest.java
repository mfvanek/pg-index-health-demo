/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

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
}
