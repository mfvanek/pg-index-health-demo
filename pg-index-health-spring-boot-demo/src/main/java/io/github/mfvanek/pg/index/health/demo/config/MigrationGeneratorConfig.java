/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationGeneratorConfig {

    @Bean
    public DbMigrationGenerator<ForeignKey> dbMigrationGenerator() {
        return new ForeignKeyMigrationGenerator(GeneratingOptions.builder().build());
    }
}
