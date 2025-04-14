/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.core.checks.common.Diagnostic;
import io.github.mfvanek.pg.core.utils.ClockHolder;
import io.github.mfvanek.pg.index.health.demo.without.spring.support.DatabaseAwareTestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthDataCollectorTest extends DatabaseAwareTestBase {

    private static final LocalDateTime MILLENNIUM = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(MILLENNIUM.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static Clock originalClock;

    @BeforeAll
    static void setUp() {
        originalClock = ClockHolder.setClock(FIXED_CLOCK);
    }

    @AfterAll
    static void tearDown() {
        if (originalClock != null) {
            ClockHolder.setClock(originalClock);
        }
    }

    @Test
    void shouldCollectHealthData() {
        final List<String> expected = List.of(
            "2000-01-01T00:00:00Z\tdb_indexes_health\tinvalid_indexes\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tduplicated_indexes\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tintersected_indexes\t2",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tunused_indexes\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tforeign_keys_without_index\t5",
            "2000-01-01T00:00:00Z\tdb_indexes_health\ttables_with_missing_indexes\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\ttables_without_primary_key\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tindexes_with_null_values\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tbloated_indexes\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tbloated_tables\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\ttables_without_description\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tcolumns_without_description\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tcolumns_with_json_type\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tcolumns_with_serial_types\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tfunctions_without_description\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tindexes_with_boolean\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tnot_valid_constraints\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tbtree_indexes_on_array_columns\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tsequence_overflow\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tprimary_keys_with_serial_types\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tduplicated_foreign_keys\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tintersected_foreign_keys\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tpossible_object_name_overflow\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\ttables_not_linked_to_others\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tforeign_keys_with_unmatched_column_type\t1",
            "2000-01-01T00:00:00Z\tdb_indexes_health\ttables_with_zero_or_one_column\t0",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tobjects_not_following_naming_convention\t2",
            "2000-01-01T00:00:00Z\tdb_indexes_health\tcolumns_not_following_naming_convention\t1");
        final List<String> healthData = HealthDataCollector.collectHealthData(getConnectionFactory(), getConnectionCredentials());
        assertThat(healthData)
            .hasSameSizeAs(Diagnostic.values())
            .containsExactlyInAnyOrder(expected.toArray(new String[0]));
    }
}
