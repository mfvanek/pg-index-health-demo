/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.health.checks.management.DatabaseManagement;
import io.github.mfvanek.pg.model.settings.PgParam;
import io.github.mfvanek.pg.model.settings.ServerSpecification;
import io.github.mfvanek.pg.model.units.MemoryUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db/configuration")
public class DbConfigurationController {

    private final DatabaseManagement databaseManagement;

    @GetMapping
    public ResponseEntity<Collection<PgParam>> getParamsWithDefaultValues() {
        final ServerSpecification serverSpecification = ServerSpecification.builder()
            .withCpuCores(Runtime.getRuntime().availableProcessors())
            .withMemoryAmount(8, MemoryUnit.GB)
            .withSSD()
            .build();
        return ResponseEntity.ok(databaseManagement.getParamsWithDefaultValues(serverSpecification));
    }
}
