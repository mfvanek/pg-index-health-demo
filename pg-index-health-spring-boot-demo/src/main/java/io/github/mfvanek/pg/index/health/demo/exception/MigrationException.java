/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.exception;

@SuppressWarnings("serial")
public class MigrationException extends IllegalStateException {

    public MigrationException(final String message) {
        super(message);
    }
}
