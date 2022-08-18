/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.utils.HealthDataCollector;
import io.github.mfvanek.pg.index.health.demo.utils.MigrationRunner;
import io.github.mfvanek.pg.index.health.demo.utils.MigrationsGenerator;
import io.github.mfvanek.pg.model.index.ForeignKey;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import javax.sql.DataSource;

@Slf4j
public class DemoApp {

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            final DataSource dataSource = embeddedPostgres.getPostgresDatabase();
            MigrationRunner.runMigrations(dataSource);
            HealthDataCollector.collectHealthData("postgres", embeddedPostgres.getPort());
            final List<ForeignKey> foreignKeys = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(dataSource);
            MigrationsGenerator.generateMigrations(dataSource, foreignKeys);
            final List<ForeignKey> afterMigrations = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(dataSource);
            if (!afterMigrations.isEmpty()) {
                throw new IllegalStateException("There should be no foreign keys not covered by the index");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
