/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.mfvanek.pg.index.health.demo.utils.StatisticsCollector.resetStatistics;

public class StatisticsDemoApp {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsDemoApp.class);

    public static void main(final String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            resetStatistics(embeddedPostgres.getPostgresDatabase());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
