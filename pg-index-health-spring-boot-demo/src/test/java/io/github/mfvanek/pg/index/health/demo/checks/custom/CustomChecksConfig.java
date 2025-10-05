/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.checks.custom;

import io.github.mfvanek.pg.connection.PgConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class CustomChecksConfig {

    @Bean
    AllDateTimeColumnsShouldEndWithAtCheckOnHost allDateTimeColumnsShouldEndWithAtCheckOnHost(final PgConnection pgConnection) {
        return new AllDateTimeColumnsShouldEndWithAtCheckOnHost(pgConnection);
    }

    @Bean
    AllPrimaryKeysMustBeNamedAsIdCheckOnHost allPrimaryKeysMustBeNamedAsIdCheckOnHost(final PgConnection pgConnection) {
        return new AllPrimaryKeysMustBeNamedAsIdCheckOnHost(pgConnection);
    }
}
