package io.github.mfvanek.pg.index.health.demo.exception;

public class MigrationException extends IllegalStateException {

    public MigrationException(String message) {
        super(message);
    }
}
