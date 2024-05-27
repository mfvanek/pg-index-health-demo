plugins {
    id("java")
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.pitest")
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.5"
    id("com.google.osdetector") version "1.7.3"
}

ext["commons-lang3.version"] = "3.13.0"

dependencies {
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.6")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.springdoc.openapi.security)
    implementation("org.liquibase:liquibase-core:4.28.0")
    implementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.11.1"))
    implementation("io.github.mfvanek:pg-index-health")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly(libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.github.mfvanek:pg-index-health-test-starter")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")
    testImplementation(libs.postgresql)

    // https://github.com/netty/netty/issues/11020
    if (osdetector.arch == "aarch_64") {
        testImplementation("io.netty:netty-all:4.1.110.Final")
    }
}

dependencyManagement {
    imports {
        // Need use this instead of 'testImplementation(platform("org.junit:junit-bom:5.10.1"))'
        // to update junit at runtime as well
        mavenBom("org.junit:junit-bom:5.10.2")
        mavenBom("org.apache.httpcomponents.client5:httpclient5-parent:5.3.1")
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
                    minimum = "0.99".toBigDecimal()
                }
            }
        }
    }
}

springBoot {
    buildInfo()
}

pitest {
    excludedClasses.set(listOf("io.github.mfvanek.pg.index.health.demo.config.*",
        "io.github.mfvanek.pg.index.health.demo.PgIndexHealthSpringBootDemoApplication"))
    excludedTestClasses.set(listOf("io.github.mfvanek.pg.index.health.demo.ActuatorEndpointTest"))
}
