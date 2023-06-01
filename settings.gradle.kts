rootProject.name = "pg-index-health-demo"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql:postgresql:42.6.0")
            library("logback-classic", "ch.qos.logback:logback-classic:1.4.7")
            val pgIndexHealth = version("pg-index-health", "0.9.3")
            library("pgIndexHealth-core", "io.github.mfvanek", "pg-index-health")
                    .versionRef(pgIndexHealth)
            library("pgIndexHealth-generator", "io.github.mfvanek", "pg-index-health-generator")
                    .versionRef(pgIndexHealth)
            library("pgIndexHealth-testing", "io.github.mfvanek", "pg-index-health-testing")
                    .versionRef(pgIndexHealth)
        }
    }
}
