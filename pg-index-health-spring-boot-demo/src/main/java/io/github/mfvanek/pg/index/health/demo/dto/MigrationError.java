package io.github.mfvanek.pg.index.health.demo.dto;

public record MigrationError(
    int statusCode,
    String message
) {

}
