/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationRequest;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class DbMigrationControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void runsMigrations() {
        final ConnectionCredentials creds = ConnectionCredentials.ofUrl(
            Objects.requireNonNull(context.getEnvironment().getProperty("spring.datasource.url")),
            Objects.requireNonNull(context.getEnvironment().getProperty("spring.datasource.userName")),
            Objects.requireNonNull(context.getEnvironment().getProperty("spring.datasource.password")));
        final ForeignKeyMigrationRequest foreignKeyMigrationRequest = new ForeignKeyMigrationRequest(creds);
        final ForeignKeyMigrationResponse result = webTestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "migration", "generate")
                .build())
            .body(BodyInserters.fromValue(foreignKeyMigrationRequest))
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ForeignKeyMigrationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).isNotNull();
    }

}
