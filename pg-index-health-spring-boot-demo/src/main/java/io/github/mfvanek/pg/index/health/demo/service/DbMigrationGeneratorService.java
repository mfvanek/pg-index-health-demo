package io.github.mfvanek.pg.index.health.demo.service;

import io.github.mfvanek.pg.connection.ConnectionCredentials;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationResponse;
import io.github.mfvanek.pg.index.health.demo.dto.ForeignKeyMigrationRequest;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DbMigrationGeneratorService {
    public ForeignKeyMigrationResponse addIndexesWithFKChecks(ForeignKeyMigrationRequest fKMigrationRequest){
        var keysBefore = getFKsFromDb(fKMigrationRequest.connectionFactory(), fKMigrationRequest.credentials());
        var migrations = generategMigrations(keysBefore);
        var keysAfter = getFKsFromDb(fKMigrationRequest.connectionFactory(), fKMigrationRequest.credentials());
        if (!keysAfter.isEmpty()) throw new RuntimeException();
        return new ForeignKeyMigrationResponse(keysBefore, keysAfter, migrations);
    }

    private List<ForeignKey> getFKsFromDb (HighAvailabilityPgConnectionFactory connectionFactory, ConnectionCredentials credentials) {
        return List.of();
    }
    private List<String> generategMigrations(List<ForeignKey> foreignKeys) {
        return List.of();
    }
}
