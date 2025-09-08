/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.github.mfvanek.pg.model.constraint.ForeignKey

data class ForeignKeyMigrationResponse(
    val foreignKeysBefore: List<ForeignKey>,
    val foreignKeysAfter: List<ForeignKey>,
    val generatedMigrations: List<String>
)
