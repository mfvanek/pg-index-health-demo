package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.connection.HighAvailabilityPgConnectionFactoryImpl;
import io.github.mfvanek.pg.connection.PgConnectionFactoryImpl;
import io.github.mfvanek.pg.index.health.IndexesHealth;
import io.github.mfvanek.pg.index.health.IndexesHealthImpl;
import io.github.mfvanek.pg.index.health.logger.Exclusions;
import io.github.mfvanek.pg.index.health.logger.IndexesHealthLogger;
import io.github.mfvanek.pg.index.health.logger.SimpleHealthLogger;
import io.github.mfvanek.pg.index.maintenance.MaintenanceFactoryImpl;
import io.github.mfvanek.pg.model.MemoryUnit;
import io.github.mfvanek.pg.model.PgContext;

public class DemoApp {

    public static void main(String[] args) {
        loadDriver();
        forTesting();
        forProduction();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void forTesting() {
        final String writeUrl = "jdbc:postgresql://host-name-1:6432,host-name-2:6432,host-name-3:6432/db_name_testing?targetServerType=master&ssl=true&prepareThreshold=0&preparedStatementCacheQueries=0&sslmode=require";
        final String readUrl = "jdbc:postgresql://host-name-1:6432,host-name-2:6432,host-name-3:6432/db_name_testing?targetServerType=preferSlave&loadBalanceHosts=true&ssl=true&prepareThreshold=0&preparedStatementCacheQueries=0&sslmode=require";
        final String userName = "user_name_testing";
        final String password = "password_testing";
        final HighAvailabilityPgConnectionFactory haPgConnectionFactory = new HighAvailabilityPgConnectionFactoryImpl(new PgConnectionFactoryImpl());
        final HighAvailabilityPgConnection haPgConnection = haPgConnectionFactory.of(writeUrl, userName, password, readUrl);
        final IndexesHealth indexesHealth = new IndexesHealthImpl(haPgConnection, new MaintenanceFactoryImpl());
        final IndexesHealthLogger logger = new SimpleHealthLogger(indexesHealth);
        logger.logAll(Exclusions.empty(), PgContext.ofPublic())
                .forEach(System.out::println);
        // Resetting current statistics
        // indexesHealth.resetStatistics();
    }

    private static void forProduction() {
        final String writeUrl = "jdbc:postgresql://host-name-1:6432,host-name-2:6432,host-name-3:6432/db_name_production?ssl=true&targetServerType=master&prepareThreshold=0&preparedStatementCacheQueries=0&connectTimeout=2&socketTimeout=50&loginTimeout=10&sslmode=require";
        final String readUrl = "jdbc:postgresql://host-name-1:6432,host-name-2:6432,host-name-3:6432,host-name-4:6432,host-name-5:6432/db_name_production?ssl=true&targetServerType=preferSlave&loadBalanceHosts=true&prepareThreshold=0&preparedStatementCacheQueries=0&connectTimeout=2&socketTimeout=50&loginTimeout=10&sslmode=require";
        final String cascadeAsyncReadUrl = "jdbc:postgresql://host-name-6:6432/db_name_production?ssl=true&targetServerType=preferSlave&loadBalanceHosts=true&prepareThreshold=0&preparedStatementCacheQueries=0&connectTimeout=2&socketTimeout=50&loginTimeout=10&sslmode=require";
        final String userName = "user_name_production";
        final String password = "password_production";
        final HighAvailabilityPgConnectionFactory haPgConnectionFactory = new HighAvailabilityPgConnectionFactoryImpl(new PgConnectionFactoryImpl());
        final HighAvailabilityPgConnection haPgConnection = haPgConnectionFactory.of(writeUrl, userName, password, readUrl, cascadeAsyncReadUrl);
        final IndexesHealth indexesHealth = new IndexesHealthImpl(haPgConnection, new MaintenanceFactoryImpl());
        final Exclusions exclusions = Exclusions.builder()
                .withIndexSizeThreshold(10, MemoryUnit.MB)
                .withTableSizeThreshold(10, MemoryUnit.MB)
                .build();
        final IndexesHealthLogger logger = new SimpleHealthLogger(indexesHealth);
        logger.logAll(exclusions, PgContext.ofPublic())
                .forEach(System.out::println);
    }
}
