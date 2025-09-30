import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.github.spotbugs.snom.SpotBugsTask
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("pg-index-health-demo.java-compilation")
    id("pg-index-health-demo.java-conventions")
    id("pg-index-health-demo.forbidden-apis")
    id("pg-index-health-demo.pitest")
    alias(libs.plugins.spring.boot.v3)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.osdetector)
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(project(":db-migrations"))
    implementation(platform(libs.spring.boot.v3.dependencies))
    implementation(platform(libs.httpclient5.parent))
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

    implementation(libs.kotlin.logging)

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
        testImplementation(libs.netty.all)
    }

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.plugins.detekt.get().version}")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:${libs.plugins.detekt.get().version}")
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

detekt {
    toolVersion = libs.plugins.detekt.get().version.toString()
    config.setFrom(file("${rootDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "21"
}
