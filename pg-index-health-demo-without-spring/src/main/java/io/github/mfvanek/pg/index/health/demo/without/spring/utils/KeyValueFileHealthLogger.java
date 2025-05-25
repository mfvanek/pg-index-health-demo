/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.utils;

import io.github.mfvanek.pg.connection.HighAvailabilityPgConnection;
import io.github.mfvanek.pg.connection.factory.ConnectionCredentials;
import io.github.mfvanek.pg.connection.factory.HighAvailabilityPgConnectionFactory;
import io.github.mfvanek.pg.core.utils.ClockHolder;
import io.github.mfvanek.pg.health.logger.AbstractHealthLogger;
import io.github.mfvanek.pg.health.logger.DatabaseChecksOnCluster;
import io.github.mfvanek.pg.health.logger.LoggingKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class KeyValueFileHealthLogger extends AbstractHealthLogger {

    private static final Logger KV_LOG = LoggerFactory.getLogger("key-value.log");

    public KeyValueFileHealthLogger(@Nonnull final ConnectionCredentials credentials,
                                    @Nonnull final HighAvailabilityPgConnectionFactory connectionFactory,
                                    @Nonnull final Function<HighAvailabilityPgConnection, DatabaseChecksOnCluster> databaseChecksFactory) {
        super(credentials, connectionFactory, databaseChecksFactory);
    }

    @Override
    protected String writeToLog(@Nonnull final LoggingKey key, final int value) {
        final String result = format(key.getKeyName(), key.getSubKeyName(), value);
        KV_LOG.info("{}", result);
        return result;
    }

    @Nonnull
    private String format(@Nonnull final String keyName, @Nonnull final String subKeyName, final int value) {
        return DateTimeFormatter.ISO_INSTANT.format(
            ZonedDateTime.now(ClockHolder.clock())) + "\t" + keyName + "\t" + subKeyName + "\t" + value;
    }
}
