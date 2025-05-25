/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.sonarqube.gradle.SonarTask

plugins {
    id("java")
    id("jacoco")
    id("org.sonarqube")
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs")
    id("net.ltgt.errorprone")
    id("io.freefair.lombok")
}

configurations.configureEach {
    exclude("org.hamcrest")
}

dependencies {
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.15.0"))
    implementation(platform("org.testcontainers:testcontainers-bom:1.21.0"))

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.postgresql:postgresql:42.7.5")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation(platform("org.assertj:assertj-bom:3.27.3"))

    errorprone("com.google.errorprone:error_prone_core:2.38.0")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.28")

    spotbugsSlf4j("org.slf4j:slf4j-simple:2.0.17")
    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.9")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("Slf4jLoggerShouldBeNonStatic", "Slf4jSignOnlyFormat")
    }
}

tasks {
    test {
        dependsOn(checkstyleMain, checkstyleTest, pmdMain, pmdTest, spotbugsMain, spotbugsTest)
    }

    withType<SpotBugsTask>().configureEach {
        reports {
            create("xml") { enabled = true }
            create("html") { enabled = true }
        }
    }

    withType<SonarTask>().configureEach {
        dependsOn(test, jacocoTestReport)
    }
}

lombok {
    version = "1.18.38"
}

checkstyle {
    toolVersion = "10.23.0"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

pmd {
    toolVersion = "7.12.0"
    isConsoleOutput = true
    ruleSetFiles = files("${rootDir}/config/pmd/pmd.xml")
    ruleSets = listOf()
}

spotbugs {
    showProgress.set(true)
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    excludeFilter.set(file("${rootDir}/config/spotbugs/exclude.xml"))
}
