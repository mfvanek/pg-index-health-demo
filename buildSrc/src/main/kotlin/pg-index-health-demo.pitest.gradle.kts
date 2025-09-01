/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

import info.solidsoft.gradle.pitest.PitestTask

plugins {
    id("java")
    id("info.solidsoft.pitest")
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-launcher")

    pitest("it.mulders.stryker:pit-dashboard-reporter:0.3.7")
}

pitest {
    verbosity = "DEFAULT"
    junit5PluginVersion = "1.2.2"
    pitestVersion = "1.19.0"
    threads = 4
    if (System.getenv("STRYKER_DASHBOARD_API_KEY") != null) {
        outputFormats.set(setOf("stryker-dashboard"))
    } else {
        outputFormats.set(setOf("HTML"))
    }
    timestampedReports = false
    exportLineCoverage = true
    mutationThreshold = 100
    pluginConfiguration.set(mapOf("stryker.moduleName" to project.name))
}

tasks {
    withType<PitestTask>().configureEach {
        mustRunAfter(test)
    }

    build {
        dependsOn("pitest")
    }
}
