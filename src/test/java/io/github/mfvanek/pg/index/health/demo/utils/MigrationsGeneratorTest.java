/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.model.index.ForeignKey;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationsGeneratorTest {

    @RegisterExtension
    private static final PreparedDbExtension EMBEDDED_POSTGRES = EmbeddedPostgresExtension.preparedDatabase(ds -> {
    });

    @BeforeAll
    static void runMigrations() {
        MigrationRunner.runMigrations(getDataSource());
    }

    @Nonnull
    private static DataSource getDataSource() {
        return EMBEDDED_POSTGRES.getTestDatabase();
    }

    @Test
    void generateMigrationsShouldWork() {
        final List<ForeignKey> foreignKeys = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(getDataSource());
        assertThat(foreignKeys)
                .hasSize(3);
        MigrationsGenerator.generateMigrations(getDataSource(), foreignKeys);
        assertThat(MigrationsGenerator.getForeignKeysNotCoveredWithIndex(getDataSource()))
                .isEmpty();
    }
}
