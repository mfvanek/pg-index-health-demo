/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.generator.IdxPosition;
import io.github.mfvanek.pg.health.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster;
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MigrationGeneratorConfig {

    @Bean
    public DbMigrationGenerator<ForeignKey> dbMigrationGenerator() {
        // Partial indexes do not cover foreign keys, so include nulls.
        // Use the idx_ prefix to avoid clashing with indexes auto-named by PostgreSQL.
        return new ForeignKeyMigrationGenerator(GeneratingOptions.builder()
            .includeNulls()
            .withIdxPosition(IdxPosition.PREFIX)
            .build());
    }

    @Bean
    public DatabaseCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex(final HighAvailabilityPgConnection haPgConnection) {
        return new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
    }
}
