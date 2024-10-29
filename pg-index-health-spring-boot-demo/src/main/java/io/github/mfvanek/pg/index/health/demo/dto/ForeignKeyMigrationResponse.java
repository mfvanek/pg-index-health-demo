/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mfvanek.pg.model.constraint.ForeignKey;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public record ForeignKeyMigrationResponse(
    List<ForeignKey> foreignKeysBefore,
    List<ForeignKey> foreignKeysAfter,
    List<String> generatedMigrations
) {

}
