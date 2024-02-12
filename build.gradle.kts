import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import info.solidsoft.gradle.pitest.PitestTask
import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarTask

plugins {
    id("java")
    id("com.github.spotbugs") version "6.0.7"
    id("checkstyle")
    id("jacoco")
    id("pmd")
    id("org.sonarqube") version "4.4.1.3373"
    id("info.solidsoft.pitest") version "1.15.0"
    id("io.freefair.lombok") version "8.4"
    id("net.ltgt.errorprone") version "3.1.0"
    id("org.gradle.test-retry") version "1.5.8"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "io.github.mfvanek"
version = "0.10.3"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.10.3"))
    implementation("io.github.mfvanek:pg-index-health")
    implementation("io.github.mfvanek:pg-index-health-generator")
    implementation("io.github.mfvanek:pg-index-health-testing")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.liquibase:liquibase-core:4.26.0")
    implementation("com.github.blagerweij:liquibase-sessionlock:1.6.9")
    implementation("org.apache.commons:commons-dbcp2:2.11.0")
    implementation(platform("org.testcontainers:testcontainers-bom:1.19.5"))
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
    testImplementation(platform("org.mockito:mockito-bom:5.10.0"))
    testImplementation("org.mockito:mockito-core")
    testImplementation(libs.logback.classic)
    testImplementation(libs.postgresql)

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    pitest("it.mulders.stryker:pit-dashboard-reporter:0.2.1")
    checkstyle("com.thomasjensen.checkstyle.addons:checkstyle-addons:7.0.1")

    errorprone("com.google.errorprone:error_prone_core:2.24.1")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.22")

    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.4")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("Slf4jLoggerShouldBeNonStatic")
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }

    test {
        useJUnitPlatform()
        dependsOn(checkstyleMain, checkstyleTest, pmdMain, pmdTest, spotbugsMain, spotbugsTest)
        maxParallelForks = 1
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification)

        retry {
            maxRetries.set(1)
            maxFailures.set(3)
            failOnPassedAfterRetry.set(false)
        }
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
        violationRules {
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
                    minimum = "0.92".toBigDecimal()
                }
            }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}

checkstyle {
    toolVersion = "10.12.5"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

pmd {
    toolVersion = "6.55.0"
    isConsoleOutput = true
    ruleSetFiles = files("config/pmd/pmd.xml")
    ruleSets = listOf()
}

spotbugs {
    showProgress.set(true)
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
}
tasks.withType<SpotBugsTask>().configureEach {
    reports {
        create("xml") { enabled = true }
        create("html") { enabled = true }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "mfvanek_pg-index-health-demo")
        property("sonar.organization", "mfvanek")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
tasks.withType<SonarTask>().configureEach {
    dependsOn(tasks.test, tasks.jacocoTestReport)
}

pitest {
    verbosity.set("DEFAULT")
    junit5PluginVersion.set("1.2.1")
    pitestVersion.set("1.15.3")
    threads.set(4)
    if (System.getenv("STRYKER_DASHBOARD_API_KEY") != null) {
        outputFormats.set(setOf("stryker-dashboard"))
    } else {
        outputFormats.set(setOf("HTML"))
    }
    timestampedReports.set(false)
    mutationThreshold.set(100)
}
tasks.withType<PitestTask>().configureEach {
    mustRunAfter(tasks.test)
}
tasks.build {
    dependsOn("pitest")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    gradleReleaseChannel = "current"
    checkConstraints = true
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
