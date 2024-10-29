/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.exception;

import java.io.Serial;

public class MigrationException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 42L;

    public MigrationException(final String message) {
        super(message);
    }
}
