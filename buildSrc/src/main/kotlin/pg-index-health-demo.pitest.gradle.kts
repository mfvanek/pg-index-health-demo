import info.solidsoft.gradle.pitest.PitestTask

plugins {
    id("java")
    id("info.solidsoft.pitest")
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-launcher")

    pitest("it.mulders.stryker:pit-dashboard-reporter:0.2.1")
}

pitest {
    verbosity = "DEFAULT"
    junit5PluginVersion = "1.2.1"
    pitestVersion = "1.15.8"
    threads = 4
    if (System.getenv("STRYKER_DASHBOARD_API_KEY") != null) {
        outputFormats.set(setOf("stryker-dashboard"))
    } else {
        outputFormats.set(setOf("HTML"))
    }
    timestampedReports = false
    exportLineCoverage = true
    mutationThreshold = 100
    pluginConfiguration.set(mapOf("stryker.moduleName" to project.name))
}

tasks {
    withType<PitestTask>().configureEach {
        mustRunAfter(test)
    }

    build {
        dependsOn("pitest")
    }
}
