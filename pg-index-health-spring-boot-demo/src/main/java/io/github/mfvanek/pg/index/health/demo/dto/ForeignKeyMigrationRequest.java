package io.github.mfvanek.pg.index.health.demo.dto;

import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;

public record ForeignKeyMigrationRequest(
    HighAvailabilityPgConnectionFactory connectionFactory,
    ConnectionCredentials credentials
) {

}
