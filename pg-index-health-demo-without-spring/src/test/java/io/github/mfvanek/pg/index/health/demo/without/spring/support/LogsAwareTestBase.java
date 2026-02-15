/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

    @NonNull
    private static <T> Logger getLogger(@NonNull final Class<T> type) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(type);
    }

    protected static <T> void registerLoggerOfType(@NonNull final Class<T> type) {
        final Logger logger = getLogger(type);
        logger.addAppender(logAppender);
        LOGGERS.add(logger);
    }

    @NonNull
    protected static List<ILoggingEvent> getLogs() {
        return List.copyOf(logAppender.list);
    }
}
