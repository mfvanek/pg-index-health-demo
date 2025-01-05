/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.dto.MigrationError;
import io.github.mfvanek.pg.index.health.demo.service.DbMigrationGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db/migration")
public class DbMigrationController {

    private final DbMigrationGeneratorService dbMigrationGeneratorService;

    @PostMapping("/generate")
    public ForeignKeyMigrationResponse generateMigrationsWithForeignKeysChecked() {
        return dbMigrationGeneratorService.generateMigrationsWithForeignKeysChecked();
    }

    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public MigrationError handleMigrationException(final IllegalStateException illegalStateException) {
        return new MigrationError(HttpStatus.EXPECTATION_FAILED.value(), "Migrations failed: " + illegalStateException.getMessage());
    }
}
