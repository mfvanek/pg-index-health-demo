/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import lombok.experimental.UtilityClass;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

@UtilityClass
class ConfigurableEnvironmentMutator {

    static final String DATASOURCE_URL_PROP_NAME = "spring.datasource.url";

    static boolean addDatasourceUrlIfNeed(final PostgreSQLContainer postgreSqlContainer,
                                          final Environment environment) {
        if (environment.getProperty(DATASOURCE_URL_PROP_NAME) == null &&
            environment instanceof ConfigurableEnvironment configurableEnvironment) {
            final MutablePropertySources mps = configurableEnvironment.getPropertySources();
            mps.addFirst(new MapPropertySource("connectionString",
                Map.ofEntries(Map.entry(DATASOURCE_URL_PROP_NAME, postgreSqlContainer.getJdbcUrl()))));
            return true;
        }
        return false;
    }
}
