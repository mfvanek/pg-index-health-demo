/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.exception;

import io.github.mfvanek.pg.index.health.demo.dto.MigrationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<MigrationError> catchMigrationException(final MigrationException migrationException) {
        return new ResponseEntity<>(new MigrationError(HttpStatus.EXPECTATION_FAILED.value(), migrationException.getMessage()), HttpStatus.EXPECTATION_FAILED);
    }
}
