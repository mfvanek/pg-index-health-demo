package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationRequest;
import io.github.mfvanek.pg.index.health.demo.service.DbMigrationGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db/migration")
public class DbMigrationController {

    private final DbMigrationGeneratorService dbMigrationGeneratorService;

    @PostMapping("/generate")
    public ForeignKeyMigrationResponse generateFKMigration(@RequestBody ForeignKeyMigrationRequest foreignKeyMigrationRequest) {
        return dbMigrationGeneratorService.addIndexesWithFKChecks(foreignKeyMigrationRequest);
    }
}
