/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.index.health.demo.without.spring.support.DatabaseAwareTestBase;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationsGeneratorTest extends DatabaseAwareTestBase {

    @AfterAll
    @SneakyThrows
    static void restoreSchema() {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("drop schema if exists demo cascade");
        }
        MigrationRunner.runMigrations(getDataSource());
    }

    @Test
    void generateMigrationsShouldWork() {
        final List<ForeignKey> foreignKeys = MigrationsGenerator.getForeignKeysNotCoveredWithIndex(getConnectionFactory(), getConnectionCredentials());
        assertThat(foreignKeys)
            .hasSize(5);
        MigrationsGenerator.generateMigrations(getDataSource(), foreignKeys);
        assertThat(MigrationsGenerator.getForeignKeysNotCoveredWithIndex(getConnectionFactory(), getConnectionCredentials()))
            .isEmpty();
    }
}
