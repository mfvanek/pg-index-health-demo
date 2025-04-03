plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:6.1.0.5360")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.1.7")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.0.1")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
    implementation("org.gradle:test-retry-gradle-plugin:1.6.2")
    implementation("io.freefair.gradle:lombok-plugin:8.13.1")
    implementation("de.thetaphi:forbiddenapis:3.8")
}
