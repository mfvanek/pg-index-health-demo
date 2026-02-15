/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PgIndexHealthSpringBootKotlinDemoApplication

fun main(args: Array<String>) {
    runApplication<PgIndexHealthSpringBootKotlinDemoApplication>(*args)
}
