/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.dto

import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing foreign key migration information")
class ForeignKeyMigrationResponse(
    @field:Schema(description = "List of foreign keys before migration")
    val foreignKeysBefore: List<ForeignKey>,
    @field:Schema(description = "List of foreign keys after migration")
    val foreignKeysAfter: List<ForeignKey>,
    @field:Schema(
        description = "List of generated migration scripts",
        example = "[\"ALTER TABLE orders ADD CONSTRAINT fk_orders_buyer_id FOREIGN KEY (buyer_id) REFERENCES buyer(id);\"]"
    )
    val generatedMigrations: List<String>
)
