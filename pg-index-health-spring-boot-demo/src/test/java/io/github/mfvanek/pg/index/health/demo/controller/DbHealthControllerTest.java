/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-spring-boot-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DbHealthControllerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Test
    void collectHealthDataShouldReturnOk() {
        final String[] result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .pathSegment("db", "health")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String[].class)
            .returnResult()
            .getResponseBody();

        assertThat(result)
            .containsExactly(
                "invalid_indexes:1",
                "duplicated_indexes:1",
                "intersected_indexes:2",
                "unused_indexes:0",
                "foreign_keys_without_index:3",
                "tables_with_missing_indexes:0",
                "tables_without_primary_key:1",
                "indexes_with_null_values:1",
                "indexes_with_bloat:0",
                "tables_with_bloat:0",
                "tables_without_description:0",
                "columns_without_description:0",
                "columns_with_json_type:0",
                "columns_with_serial_types:0",
                "functions_without_description:0",
                "indexes_with_boolean:1",
                "not_valid_constraints:1",
                "btree_indexes_on_array_columns:1");
    }
}
