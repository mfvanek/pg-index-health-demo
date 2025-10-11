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
                "duplicated_indexes:2",
                "foreign_keys_without_index:5",
                "bloated_indexes:0",
                "indexes_with_null_values:1",
                "intersected_indexes:3",
                "invalid_indexes:1",
                "unused_indexes:8",
                "tables_without_description:0",
                "columns_without_description:1",
                "columns_with_json_type:0",
                "columns_with_serial_types:0",
                "functions_without_description:0",
                "indexes_with_boolean:1",
                "not_valid_constraints:1",
                "btree_indexes_on_array_columns:1",
                "sequence_overflow:1",
                "primary_keys_with_serial_types:1",
                "duplicated_foreign_keys:1",
                "intersected_foreign_keys:0",
                "possible_object_name_overflow:1",
                "tables_not_linked_to_others:0",
                "foreign_keys_with_unmatched_column_type:1",
                "tables_with_zero_or_one_column:0",
                "objects_not_following_naming_convention:3",
                "columns_not_following_naming_convention:1",
                "primary_keys_with_varchar:0",
                "columns_with_fixed_length_varchar:12",
                "indexes_with_unnecessary_where_clause:0",
                "primary_keys_that_most_likely_natural_keys:0",
                "columns_with_money_type:0",
                "indexes_with_timestamp_in_the_middle:0",
                "columns_with_timestamp_or_timetz_type:0",
                "tables_where_primary_key_columns_not_first:0",
                "tables_where_all_columns_nullable_except_pk:0"
            );
    }
}
