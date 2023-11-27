rootProject.name = "pg-index-health-demo"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql:postgresql:42.7.0")
            library("logback-classic", "ch.qos.logback:logback-classic:1.4.11")
        }
    }
}
