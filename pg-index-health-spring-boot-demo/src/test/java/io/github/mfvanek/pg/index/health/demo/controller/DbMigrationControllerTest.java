/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.dto.MigrationError;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DbMigrationControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Test
    void runsMigrations() {
        final ForeignKeyMigrationResponse result = webTestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "migration", "generate")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ForeignKeyMigrationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).isNotNull();
        assertThat(result.foreignKeysBefore().isEmpty()).isFalse();
        assertThat(result.foreignKeysAfter().isEmpty()).isTrue();
        assertThat(result.generatedMigrations()).allMatch(s -> s.contains("create index concurrently if not exists"));
    }

    @Test
    void returnsNothingWithWrongAuthorization() {
        final MigrationError result = webTestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "migration", "generate")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody(MigrationError.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).isNull();
    }
}
