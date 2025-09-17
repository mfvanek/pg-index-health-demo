/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection
import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator
import io.github.mfvanek.pg.generator.GeneratingOptions
import io.github.mfvanek.pg.health.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class MigrationGeneratorConfig {

    @Bean
    fun dbMigrationGenerator(): DbMigrationGenerator<ForeignKey> {
        return ForeignKeyMigrationGenerator(GeneratingOptions.builder().build())
    }

    @Bean
    fun foreignKeysNotCoveredWithIndex(
        haPgConnection: HighAvailabilityPgConnection
    ): DatabaseCheckOnCluster<ForeignKey> {
        return ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection)
    }
}
