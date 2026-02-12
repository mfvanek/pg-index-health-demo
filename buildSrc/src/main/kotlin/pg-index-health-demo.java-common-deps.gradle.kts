/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

plugins {
    id("java")
}

configurations.configureEach {
    exclude("org.hamcrest")
}

dependencies {
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.30.2"))
    implementation(platform("org.testcontainers:testcontainers-bom:1.21.4"))

    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.postgresql:postgresql:42.7.10")

    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation(platform("org.assertj:assertj-bom:3.27.7"))
}
