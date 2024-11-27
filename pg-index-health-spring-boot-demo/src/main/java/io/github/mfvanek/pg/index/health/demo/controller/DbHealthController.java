/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.health.logger.Exclusions;
import io.github.mfvanek.pg.health.logger.HealthLogger;
import io.github.mfvanek.pg.model.context.PgContext;
import io.github.mfvanek.pg.model.units.MemoryUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db/health")
public class DbHealthController {

    private final HealthLogger healthLogger;
    private final PgContext pgContext;

    @GetMapping
    public ResponseEntity<Collection<String>> collectHealthData() {
        final Exclusions exclusions = Exclusions.builder()
            .withIndexSizeThreshold(1, MemoryUnit.MB)
            .withTableSizeThreshold(1, MemoryUnit.MB)
            .build();
        return ResponseEntity.ok(healthLogger.logAll(exclusions, pgContext));
    }
}
