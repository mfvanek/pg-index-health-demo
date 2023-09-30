rootProject.name = "pg-index-health-demo"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql:postgresql:42.6.0")
            library("logback-classic", "ch.qos.logback:logback-classic:1.4.11")
            val pgIndexHealth = version("pg-index-health", "0.9.5")
            library("pg-index-health-core", "io.github.mfvanek", "pg-index-health")
                    .versionRef(pgIndexHealth)
            library("pg-index-health-generator", "io.github.mfvanek", "pg-index-health-generator")
                    .versionRef(pgIndexHealth)
            library("pg-index-health-testing", "io.github.mfvanek", "pg-index-health-testing")
                    .versionRef(pgIndexHealth)
            version("checkstyle", "10.12.0")
            version("pmd", "6.55.0")
            version("jacoco", "0.8.10")
            library("pitest-dashboard-reporter", "it.mulders.stryker:pit-dashboard-reporter:0.2.1")
            version("pitest-junit5Plugin", "1.2.0")
            version("pitest-core", "1.14.1")
        }
    }
}
