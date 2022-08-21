/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.utils.ConfigurationCollector;
import io.github.mfvanek.pg.index.health.demo.utils.PostgreSqlContainerWrapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigurationDemoApp {

    @SneakyThrows
    public static void main(final String[] args) {
        try (PostgreSqlContainerWrapper postgres = new PostgreSqlContainerWrapper("13.7")) {
            ConfigurationCollector.checkConfig(postgres.getDataSource());
        }
    }
}
