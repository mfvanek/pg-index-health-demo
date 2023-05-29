import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import info.solidsoft.gradle.pitest.PitestTask
import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarTask

plugins {
    id("java")
    id("com.github.spotbugs") version "5.0.14"
    id("checkstyle")
    id("jacoco")
    id("pmd")
    id("org.sonarqube") version "4.0.0.2929"
    id("info.solidsoft.pitest") version "1.9.11"
    id("io.freefair.lombok") version "8.0.1"
    id("net.ltgt.errorprone") version "3.1.0"
    id("org.gradle.test-retry") version "1.5.2"
}

group = "io.github.mfvanek"
version = "0.9.3-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

val pgihVersion = "0.9.3"
val logbackVersion = "1.4.7"
val postgresqlVersion = "42.6.0"

dependencies {
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.mfvanek:pg-index-health:$pgihVersion")
    implementation("io.github.mfvanek:pg-index-health-generator:$pgihVersion")
    implementation("io.github.mfvanek:pg-index-health-testing:$pgihVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.liquibase:liquibase-core:4.22.0")
    implementation("org.apache.commons:commons-dbcp2:2.9.0")
    implementation(enforcedPlatform("org.testcontainers:testcontainers-bom:1.18.1"))
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:postgresql")

    testImplementation(enforcedPlatform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.postgresql:postgresql:$postgresqlVersion")

    pitest("it.mulders.stryker:pit-dashboard-reporter:0.2.1")
    checkstyle("com.thomasjensen.checkstyle.addons:checkstyle-addons:7.0.1")
    errorprone("com.google.errorprone:error_prone_core:2.19.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks {
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
        dependsOn(test)
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
        dependsOn(jacocoTestReport, jacocoTestCoverageVerification)
    }
}

checkstyle {
    toolVersion = "10.7.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.54.0"
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

sonarqube {
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
    junit5PluginVersion.set("1.1.2")
    pitestVersion.set("1.10.4")
    threads.set(4)
    if (System.getenv("STRYKER_DASHBOARD_API_KEY") != null) {
        outputFormats.set(setOf("stryker-dashboard"))
    } else {
        outputFormats.set(setOf("HTML"))
    }
    timestampedReports.set(false)
    mutationThreshold.set(100)
    excludedClasses.set(listOf("io.github.mfvanek.pg.index.health.demo.utils.PostgreSqlContainerWrapper"))
}
tasks.withType<PitestTask>().configureEach {
    mustRunAfter(tasks.test)
}
tasks.build {
    dependsOn("pitest")
}
