/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.dto.MigrationError;
import io.github.mfvanek.pg.index.health.demo.service.DbMigrationGeneratorService;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DbMigrationControllerMockTest extends BasePgIndexHealthDemoSpringBootTest {

    @MockBean
    DbMigrationGeneratorService dbMigrationGeneratorService;

    @Test
    void returnsMigrationErrorWhenKeysAfterAreNotEmpty() {
        final IllegalStateException illegalStateException = new IllegalStateException("There should be no foreign keys not covered by the index");
        Mockito.when(dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked())
            .thenThrow(illegalStateException);

        final MigrationError result = webTestClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "migration", "generate")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.EXPECTATION_FAILED)
            .expectBody(MigrationError.class)
            .returnResult()
            .getResponseBody();

        assertThat(result)
            .isNotNull()
            .isInstanceOf(MigrationError.class);
        assertThat(result.message()).contains("Migrations failed: There should be no foreign keys not covered by the index");
    }
}
