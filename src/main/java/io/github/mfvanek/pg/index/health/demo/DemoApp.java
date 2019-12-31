package io.github.mfvanek.pg.index.health.demo;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionImpl;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.index.health.IndexesHealth;
import io.github.mfvanek.pg.index.health.IndexesHealthImpl;
import io.github.mfvanek.pg.index.health.logger.Exclusions;
import io.github.mfvanek.pg.index.health.logger.IndexesHealthLogger;
import io.github.mfvanek.pg.index.health.logger.SimpleHealthLogger;
import io.github.mfvanek.pg.index.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DemoApp {

    public static void main(String[] args) {
        try (EmbeddedPostgres embeddedPostgres = EmbeddedPostgres.start()) {
            collectHealthData(embeddedPostgres);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void collectHealthData(@Nonnull EmbeddedPostgres embeddedPostgres) {
        final PgConnection pgConnection = PgConnectionImpl.ofMaster(embeddedPostgres.getPostgresDatabase());
        final HighAvailabilityPgConnection haPgConnection = HighAvailabilityPgConnectionImpl.of(pgConnection);
        final IndexesHealth indexesHealth = new IndexesHealthImpl(haPgConnection, new MaintenanceFactoryImpl());
        final Exclusions exclusions = Exclusions.builder()
                .withIndexSizeThreshold(1, MemoryUnit.MB)
                .withTableSizeThreshold(1, MemoryUnit.MB)
                .build();
        final IndexesHealthLogger logger = new SimpleHealthLogger(indexesHealth);
        logger.logAll(exclusions, PgContext.ofPublic())
                .forEach(System.out::println);
    }
}
