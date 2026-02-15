/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.exception

/**
 * Exception thrown when database migration operations fail.
 *
 * @property message the detail message
 * @property cause the cause of this exception
 */
class MigrationException : RuntimeException {
    constructor(message: String) : super(message)
}
