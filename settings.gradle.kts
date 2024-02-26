rootProject.name = "pg-index-health-demo"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql:postgresql:42.7.2")
            library("logback-classic", "ch.qos.logback:logback-classic:1.5.0")
        }
    }
}
