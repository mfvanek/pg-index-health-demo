/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.index.health.demo.DatabaseAwareTestBase;
import io.zonky.test.db.postgres.embedded.ConnectionInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthDataCollectorTest extends DatabaseAwareTestBase {

    @Test
    void shouldCollectHealthData() {
        final List<String> expected = Arrays.asList(
                "db_indexes_health\tinvalid_indexes\t1",
                "db_indexes_health\tduplicated_indexes\t1",
                "db_indexes_health\tintersected_indexes\t2",
                "db_indexes_health\tunused_indexes\t0",
                "db_indexes_health\tforeign_keys_without_index\t3",
                "db_indexes_health\ttables_with_missing_indexes\t0",
                "db_indexes_health\ttables_without_primary_key\t1",
                "db_indexes_health\tindexes_with_null_values\t1",
                "db_indexes_health\tindexes_bloat\t0",
                "db_indexes_health\ttables_bloat\t0");
        final ConnectionInfo info = EMBEDDED_POSTGRES.getConnectionInfo();
        final List<String> healthData = HealthDataCollector.collectHealthData(info.getDbName(), info.getPort());
        assertThat(healthData)
                .isNotNull()
                .hasSize(10)
                .matches(l -> l.stream().allMatch(s -> expected.stream().anyMatch(s::contains)));
    }
}
