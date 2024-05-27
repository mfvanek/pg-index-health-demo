/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-spring-boot-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import io.github.mfvanek.pg.index.health.demo.service.StatisticsCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db/statistics")
public class DbStatisticsController {

    private final StatisticsCollectorService statisticsCollectorService;

    @GetMapping("/reset")
    public ResponseEntity<OffsetDateTime> getLastResetDate() {
        return ResponseEntity.ok(statisticsCollectorService.getLastStatsResetTimestamp());
    }

    @PostMapping("/reset")
    public ResponseEntity<OffsetDateTime> doReset(@RequestBody final boolean wait) {
        if (wait) {
            return ResponseEntity.ok().body(statisticsCollectorService.resetStatistics());
        }

        if (!statisticsCollectorService.resetStatisticsNoWait()) {
            throw new IllegalStateException("Could not reset statistics");
        }
        return ResponseEntity.accepted().body(statisticsCollectorService.getLastStatsResetTimestamp());
    }
}
