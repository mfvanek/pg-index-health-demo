plugins {
    id("java")
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.forbidden-apis")
    id("pg-index-health-demo.pitest")
    alias(libs.plugins.spring.boot.v3)
    alias(libs.plugins.osdetector)
}

dependencies {
    implementation(project(":db-migrations"))
    implementation(platform(libs.spring.boot.v3.dependencies))
    implementation(platform(libs.springdoc.openapi.bom))

    implementation(libs.commons.lang3)

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-logger")
    implementation("io.github.mfvanek:pg-index-health-generator")
    implementation("io.github.mfvanek:pg-index-health-model-jackson-module")
    implementation(libs.liquibase.core)
    implementation(libs.liquibase.sessionlock)

    annotationProcessor(platform(libs.spring.boot.v3.dependencies))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.github.mfvanek:pg-index-health-test-starter")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")

    // https://github.com/netty/netty/issues/11020
    if (osdetector.arch == "aarch_64") {
        testImplementation(libs.netty.all)
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
                    counter = "CLASS"
                    value = "MISSEDCOUNT"
                    maximum = "0.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "METHOD"
                    value = "MISSEDCOUNT"
                    maximum = "0.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "LINE"
                    value = "MISSEDCOUNT"
                    maximum = "1.0".toBigDecimal()
                }
            }
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
                    minimum = "0.95".toBigDecimal()
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
