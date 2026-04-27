plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:7.2.3.7755")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.5.1")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.19.0")
    implementation("org.gradle:test-retry-gradle-plugin:1.6.4")
    implementation("io.freefair.gradle:lombok-plugin:9.2.0")
    implementation("de.thetaphi:forbiddenapis:3.10")
    val kotlinVersion = "2.3.21"
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:$kotlinVersion")
    implementation(libs.detekt)
}
