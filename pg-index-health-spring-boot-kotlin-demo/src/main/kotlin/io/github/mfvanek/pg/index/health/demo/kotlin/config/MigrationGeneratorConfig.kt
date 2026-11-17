/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection
import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator
import io.github.mfvanek.pg.generator.GeneratingOptions
import io.github.mfvanek.pg.generator.IdxPosition
import io.github.mfvanek.pg.health.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class MigrationGeneratorConfig {

    // Partial indexes do not cover foreign keys, so include nulls.
    // Use the idx_ prefix to avoid clashing with indexes auto-named by PostgreSQL.
    @Bean
    fun dbMigrationGenerator(): DbMigrationGenerator<ForeignKey> = ForeignKeyMigrationGenerator(
        GeneratingOptions.builder()
            .includeNulls()
            .withIdxPosition(IdxPosition.PREFIX)
            .build()
    )

    @Bean
    fun foreignKeysNotCoveredWithIndex(
        haPgConnection: HighAvailabilityPgConnection,
    ): DatabaseCheckOnCluster<ForeignKey> = ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection)
}
