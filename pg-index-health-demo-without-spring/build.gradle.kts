plugins {
    id("java")
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.pitest")
}

dependencies {
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.11.1"))
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-generator")
    implementation("io.github.mfvanek:pg-index-health-testing")

    implementation("org.liquibase:liquibase-core:4.28.0")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")
    implementation("org.apache.commons:commons-dbcp2:2.12.0")
    implementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.logback.classic)

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(platform("org.assertj:assertj-bom:3.25.3"))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation(platform("org.mockito:mockito-bom:5.12.0"))
    testImplementation("org.mockito:mockito-core")
    testImplementation(libs.logback.classic)
    testImplementation(libs.postgresql)

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
