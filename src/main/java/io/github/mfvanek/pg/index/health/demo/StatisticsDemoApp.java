/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import static io.github.mfvanek.pg.index.health.demo.utils.StatisticsCollector.resetStatistics;

@UtilityClass
public class StatisticsDemoApp {

    @SneakyThrows
    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            resetStatistics(embeddedPostgres.getPostgresDatabase());
        }
    }
}
