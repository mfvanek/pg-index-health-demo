import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.forbidden-apis")
    id("pg-index-health-demo.pitest")
    alias(libs.plugins.spring.boot.v3)
    kotlin("plugin.spring") version "2.2.20"
    id("com.google.osdetector") version "1.7.3"
}

dependencies {
    implementation(project(":db-migrations"))
    implementation(platform(libs.spring.boot.v3.dependencies))
    implementation(platform("org.apache.httpcomponents.client5:httpclient5-parent:5.5"))
    implementation(platform(libs.springdoc.openapi.bom))

    implementation(libs.commons.lang3)

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-logger")
    implementation("io.github.mfvanek:pg-index-health-generator")
    implementation(libs.liquibase.core)
    implementation(libs.liquibase.sessionlock)

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("io.github.mfvanek:pg-index-health-test-starter")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")

    // https://github.com/netty/netty/issues/11020
    if (osdetector.arch == "aarch_64") {
        testImplementation("io.netty:netty-all:4.2.4.Final")
    }
}

tasks {
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    
    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        afterEvaluate {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/PgIndexHealthSpringBootKotlinDemoApplicationKt.class")
                }
            }))
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            classDirectories.setFrom(jacocoTestReport.get().classDirectories)
        }
    }

    // Disable SpotBugs tasks for Kotlin demo
    withType<SpotBugsTask>().configureEach {
        enabled = false
    }
}

springBoot {
    buildInfo()
}

pitest {
    // Prevent Pitest from removing calls to Kotlin internal methods
    avoidCallsTo.set(
        listOf(
            "kotlin.jvm.internal",
            "kotlin.jdk7"
        )
    )
}
