/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring;

import io.github.mfvanek.pg.index.health.demo.without.spring.utils.ConfigurationCollector;
import io.github.mfvanek.pg.index.health.demo.without.spring.utils.Consts;
import io.github.mfvanek.pg.testing.PostgreSqlContainerWrapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigurationDemoApp {

    @SneakyThrows
    public static void main(final String[] args) {
        try (PostgreSqlContainerWrapper postgres = PostgreSqlContainerWrapper.withVersion(Consts.PG_VERSION)) {
            ConfigurationCollector.checkConfig(postgres.getDataSource(), postgres.getUrl());
        }
    }
}
