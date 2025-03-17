plugins {
    id("java")
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.forbidden-apis")
    id("pg-index-health-demo.pitest")
}

dependencies {
    implementation(project(":db-migrations"))
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-generator")
    implementation("io.github.mfvanek:pg-index-health-testing")
    implementation("io.github.mfvanek:pg-index-health-logger")

    implementation("org.liquibase:liquibase-core:4.31.1")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")
    implementation("org.apache.commons:commons-dbcp2:2.13.0")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation(platform("org.mockito:mockito-bom:5.16.1"))
    testImplementation("org.mockito:mockito-core")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
