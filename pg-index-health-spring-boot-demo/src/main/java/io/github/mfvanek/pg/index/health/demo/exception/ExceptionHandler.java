package io.github.mfvanek.pg.index.health.demo.exception;

import io.github.mfvanek.pg.index.health.demo.dto.MigrationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<MigrationError> catchMigrationException(MigrationException e) {
        return new ResponseEntity<>(new MigrationError(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage()), HttpStatus.EXPECTATION_FAILED);
    }
}
