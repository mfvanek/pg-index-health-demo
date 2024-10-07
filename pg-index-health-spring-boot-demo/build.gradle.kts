plugins {
    id("java")
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.forbidden-apis")
    id("pg-index-health-demo.pitest")
    id("org.springframework.boot") version "2.7.18"
    id("com.google.osdetector") version "1.7.3"
}

dependencies {
    implementation(project(":db-migrations"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    implementation(platform("org.apache.httpcomponents.client5:httpclient5-parent:5.4"))

    implementation("org.apache.commons:commons-lang3:3.17.0")

    implementation("io.micrometer:micrometer-registry-prometheus:1.12.6")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(platform("org.springdoc:springdoc-openapi:1.8.0"))
    implementation("org.springdoc:springdoc-openapi-ui") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        because("to be compatible with Spring Boot 2.7.18")
    }
    implementation("org.springdoc:springdoc-openapi-security") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        because("to be compatible with Spring Boot 2.7.18")
    }
    implementation("org.liquibase:liquibase-core:4.29.2")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-logger")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")

    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.github.mfvanek:pg-index-health-test-starter")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")

    // https://github.com/netty/netty/issues/11020
    if (osdetector.arch == "aarch_64") {
        testImplementation("io.netty:netty-all:4.1.114.Final")
    }
}

tasks {
    withType<JacocoReport> {
        afterEvaluate {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/PgIndexHealthSpringBootDemoApplication.class")
                }
            }))
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            classDirectories.setFrom(jacocoTestReport.get().classDirectories)
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.97".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "1.00".toBigDecimal()
                }
            }
        }
    }
}

springBoot {
    buildInfo()
}

pitest {
    excludedClasses.set(
        listOf(
            "io.github.mfvanek.pg.index.health.demo.config.*",
            "io.github.mfvanek.pg.index.health.demo.PgIndexHealthSpringBootDemoApplication"
        )
    )
    excludedTestClasses.set(listOf("io.github.mfvanek.pg.index.health.demo.ActuatorEndpointTest"))
}
