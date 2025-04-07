/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
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
            .headers(this::setUpBasicAuth)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String[].class)
            .returnResult()
            .getResponseBody();

        assertThat(result)
            .containsExactly(
                "bloated_tables:0",
                "tables_with_missing_indexes:0",
                "tables_without_primary_key:1",
                "duplicated_indexes:1",
                "foreign_keys_without_index:5",
                "bloated_indexes:0",
                "indexes_with_null_values:0",
                "intersected_indexes:2",
                "invalid_indexes:1",
                "unused_indexes:0",
                "tables_without_description:0",
                "columns_without_description:0",
                "columns_with_json_type:0",
                "columns_with_serial_types:0",
                "functions_without_description:0",
                "indexes_with_boolean:0",
                "not_valid_constraints:1",
                "btree_indexes_on_array_columns:0",
                "sequence_overflow:1",
                "primary_keys_with_serial_types:1",
                "duplicated_foreign_keys:1",
                "intersected_foreign_keys:0",
                "possible_object_name_overflow:1",
                "tables_not_linked_to_others:0",
                "foreign_keys_with_unmatched_column_type:1",
                "tables_with_zero_or_one_column:0");
    }
}
