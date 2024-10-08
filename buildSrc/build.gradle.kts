plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.0.0.4638")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.15")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.1.0")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
    implementation("org.gradle:test-retry-gradle-plugin:1.5.9")
    implementation("io.freefair.gradle:lombok-plugin:8.10.2")
    implementation("de.thetaphi:forbiddenapis:3.7")
}
