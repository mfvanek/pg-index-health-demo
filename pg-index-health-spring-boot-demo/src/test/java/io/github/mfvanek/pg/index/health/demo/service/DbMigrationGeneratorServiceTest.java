/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.service;

import io.github.mfvanek.pg.generator.DbMigrationGenerator;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class DbMigrationGeneratorServiceTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    DbMigrationGeneratorService dbMigrationGeneratorService;

    @MockBean
    DbMigrationGenerator<ForeignKey> dbMigrationGenerator;

    @Test
    void throwsIllegalStateExceptionWhenEmptyMigrationString(@Nonnull final CapturedOutput output) {
        final List<ForeignKey> foreignKeys = dbMigrationGeneratorService.getForeignKeysFromDb();
        Mockito.when(dbMigrationGenerator.generate(foreignKeys)).thenReturn(List.of());

        assertThatThrownBy(dbMigrationGeneratorService::generateMigrationsWithForeignKeysChecked)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("There should be no foreign keys not covered by the index");
        assertThat(output).contains("Generated migrations: []");
    }

    @Test
    void logsAboutSqlExceptionWhenBadMigrationStringAndThrowsExceptionAfter(@Nonnull final CapturedOutput output) {
        final List<ForeignKey> foreignKeys = dbMigrationGeneratorService.getForeignKeysFromDb();
        Mockito.when(dbMigrationGenerator.generate(foreignKeys)).thenReturn(List.of("select * from payments"));

        assertThatThrownBy(dbMigrationGeneratorService::generateMigrationsWithForeignKeysChecked)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("There should be no foreign keys not covered by the index");
        assertThat(output).contains("Error running migration");
    }
}
