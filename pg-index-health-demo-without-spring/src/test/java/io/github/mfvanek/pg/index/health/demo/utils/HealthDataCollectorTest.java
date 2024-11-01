/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.common.maintenance.Diagnostic;
import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthDataCollectorTest extends DatabaseAwareTestBase {

    @Test
    void shouldCollectHealthData() {
        final List<String> expected = List.of(
            "db_indexes_health\tinvalid_indexes\t1",
            "db_indexes_health\tduplicated_indexes\t1",
            "db_indexes_health\tintersected_indexes\t2",
            "db_indexes_health\tunused_indexes\t0",
            "db_indexes_health\tforeign_keys_without_index\t5",
            "db_indexes_health\ttables_with_missing_indexes\t0",
            "db_indexes_health\ttables_without_primary_key\t1",
            "db_indexes_health\tindexes_with_null_values\t1",
            "db_indexes_health\tindexes_with_bloat\t0",
            "db_indexes_health\ttables_with_bloat\t0",
            "db_indexes_health\ttables_without_description\t0",
            "db_indexes_health\tcolumns_without_description\t0",
            "db_indexes_health\tcolumns_with_json_type\t0",
            "db_indexes_health\tcolumns_with_serial_types\t0",
            "db_indexes_health\tfunctions_without_description\t0",
            "db_indexes_health\tindexes_with_boolean\t1",
            "db_indexes_health\tnot_valid_constraints\t1",
            "db_indexes_health\tbtree_indexes_on_array_columns\t1",
            "db_indexes_health\tsequence_overflow\t1",
            "db_indexes_health\tprimary_keys_with_serial_types\t1",
            "db_indexes_health\tduplicated_foreign_keys\t1",
            "db_indexes_health\tintersected_foreign_keys\t0",
            "db_indexes_health\tpossible_object_name_overflow\t1",
            "db_indexes_health\ttables_not_linked_to_others\t0",
            "db_indexes_health\tforeign_keys_with_unmatched_column_type\t1");
        final List<String> healthData = HealthDataCollector.collectHealthData(getConnectionFactory(), getConnectionCredentials());
        assertThat(healthData)
            .hasSameSizeAs(Diagnostic.values())
            .matches(l -> l.stream().allMatch(s -> expected.stream().anyMatch(s::contains)));
    }
}
