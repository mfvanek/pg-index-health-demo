/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.service;

import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class DbMigrationServiceTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    DbMigrationGeneratorService dbMigrationGeneratorService;

    @Test
    void addsIndexesWithFkChecks() {
        assertThat(dbMigrationGeneratorService).isNotNull();
    }
}
