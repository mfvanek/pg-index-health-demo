import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("java")
    id("org.sonarqube")
    id("jacoco-report-aggregation")
    id("com.github.ben-manes.versions") version "0.52.0"
}

allprojects {
    group = "io.github.mfvanek"
    version = "0.20.2"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

dependencies {
    subprojects.forEach {
        if (it.name.contains("-demo")) {
            jacocoAggregation(it)
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.14.2"
    }

    check {
        dependsOn(named<JacocoReport>("testCodeCoverageReport"))
    }

    // To avoid creation of jar's in build folder in the root
    jar {
        isEnabled = false
    }
}

sonar {
    properties {
        property("sonar.projectKey", "mfvanek_pg-index-health-demo")
        property("sonar.organization", "mfvanek")
        property("sonar.host.url", "https://sonarcloud.io")
    }
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
