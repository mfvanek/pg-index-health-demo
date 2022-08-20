/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.checks.cluster.ForeignKeysNotCoveredWithIndexCheckOnCluster;
import io.github.mfvanek.pg.common.maintenance.AbstractCheckOnCluster;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.generator.DbMigrationGeneratorImpl;
import io.github.mfvanek.pg.generator.GeneratingOptions;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.index.ForeignKey;
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

    public static List<ForeignKey> getForeignKeysNotCoveredWithIndex(@Nonnull final DataSource dataSource) {
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(PgConnectionImpl.ofPrimary(dataSource));
        final AbstractCheckOnCluster<ForeignKey> foreignKeysNotCoveredWithIndex = new ForeignKeysNotCoveredWithIndexCheckOnCluster(haPgConnection);
        return foreignKeysNotCoveredWithIndex.check(PgContext.of("demo"));
    }

    @SneakyThrows
    public static void generateMigrations(@Nonnull final DataSource dataSource, @Nonnull final List<ForeignKey> foreignKeys) {
        final DbMigrationGenerator generator = new DbMigrationGeneratorImpl();
        final String generatedMigrations = generator.generate(foreignKeys, GeneratingOptions.builder().build());
        log.info(generatedMigrations);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(generatedMigrations);
        }
    }
}
