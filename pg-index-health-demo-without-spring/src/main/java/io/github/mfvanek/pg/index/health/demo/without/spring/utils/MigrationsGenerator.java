/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.ForeignKeyMigrationGenerator;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.health.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster;
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.model.context.PgContext;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

@Slf4j
@UtilityClass
public class MigrationsGenerator {

    public static List<ForeignKey> getForeignKeysNotCoveredWithIndex(@Nonnull final HighAvailabilityPgConnectionFactory connectionFactory,
                                                                     @Nonnull final ConnectionCredentials credentials) {
        final HighAvailabilityPgConnection haPgConnection = connectionFactory.of(credentials);
        final DatabaseCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex = new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
        return foreignKeysNotCoveredWithIndex.check(PgContext.of("demo"));
    }

    @SuppressWarnings("StringSplitter")
    @SneakyThrows
    public static void generateMigrations(@Nonnull final DataSource dataSource, @Nonnull final List<ForeignKey> foreignKeys) {
        final DbMigrationGenerator<ForeignKey> generator = new ForeignKeyMigrationGenerator(GeneratingOptions.builder().build());
        final List<String> generatedMigrations = generator.generate(foreignKeys);
        log.info("Generated migrations: {}", generatedMigrations);
        try (Connection connection = dataSource.getConnection()) {
            for (final String migration : generatedMigrations) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(migration);
                }
            }
        }
    }
}
