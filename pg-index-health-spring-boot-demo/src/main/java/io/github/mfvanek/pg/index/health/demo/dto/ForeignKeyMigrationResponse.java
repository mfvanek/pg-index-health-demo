package io.github.mfvanek.pg.index.health.demo.dto;

import io.github.mfvanek.pg.model.constraint.ForeignKey;

import java.util.List;

public record ForeignKeyMigrationResponse(
    List<ForeignKey> foreignKeysBefore,
    List<ForeignKey> foreignKeysAfter,
    List<String> generatedMigrations
) {

}
