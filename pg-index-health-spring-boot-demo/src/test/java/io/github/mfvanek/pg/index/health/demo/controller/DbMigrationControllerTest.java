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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

class DbMigrationControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    private JdbcDatabaseContainer<?> jdbcDatabaseContainer;

    @Test
    void runsMigrations() {
        final ConnectionCredentials credentials = ConnectionCredentials.ofUrl(
            jdbcDatabaseContainer.getJdbcUrl(),
            jdbcDatabaseContainer.getUsername(),
            jdbcDatabaseContainer.getPassword());
        final ForeignKeyMigrationRequest foreignKeyMigrationRequest = new ForeignKeyMigrationRequest(credentials);
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
        assertThat(result.foreignKeysBefore()).isNotEmpty();
        assertThat(result.foreignKeysAfter()).isEmpty();
        assertThat(result.generatedMigrations()).allMatch(s -> s.contains("create index concurrently if not exists"));
    }

    @AfterEach
    void truncateTables() {
        jdbcTemplate.execute("truncate table demo.buyer, demo.courier,  demo.payment, demo.order_item, demo.orders");
    }
}
