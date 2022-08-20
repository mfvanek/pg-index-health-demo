/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class LogsAwareTestBase {

    private static final List<Logger> LOGGERS = new ArrayList<>();
    private static ListAppender<ILoggingEvent> logAppender;

    @BeforeAll
    static void initLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
    }

    @AfterAll
    static void tearDown() {
        LOGGERS.clear();
        logAppender.stop();
    }

    @BeforeEach
    void cleanLogAppenderAndSetLogLevel() {
        logAppender.clearAllFilters();
        logAppender.list.clear();
        LOGGERS.forEach(l -> l.setLevel(Level.INFO));
    }

    @Nonnull
    private static <T> Logger getLogger(@Nonnull final Class<T> type) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(type);
    }

    protected static <T> void registerLoggerOfType(@Nonnull final Class<T> type) {
        final Logger logger = getLogger(type);
        logger.addAppender(logAppender);
        LOGGERS.add(logger);
    }

    @Nonnull
    protected static List<ILoggingEvent> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logAppender.list));
    }
}
