/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationRequest;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.service.DbMigrationGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressFBWarnings("EI_EXPOSE_REP2")
@RequiredArgsConstructor
@RestController
@RequestMapping("/db/migration")
public class DbMigrationController {

    private final DbMigrationGeneratorService dbMigrationGeneratorService;

    @PostMapping("/generate")
    public ForeignKeyMigrationResponse generateFkMigration(@RequestBody final ForeignKeyMigrationRequest foreignKeyMigrationRequest) {
        return dbMigrationGeneratorService.addIndexesWithFkChecks(foreignKeyMigrationRequest);
    }
}
