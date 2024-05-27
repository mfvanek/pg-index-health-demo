rootProject.name = "pg-index-health-demo"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("postgresql", "org.postgresql:postgresql:42.7.3")
            library("logback-classic", "ch.qos.logback:logback-classic:1.5.6")
            val springdoc = version("springdoc", "1.8.0")
            library("springdoc-openapi-ui", "org.springdoc", "springdoc-openapi-ui")
                .versionRef(springdoc)
            library("springdoc-openapi-security", "org.springdoc", "springdoc-openapi-security")
                .versionRef(springdoc)
        }
    }
}

include("pg-index-health-demo-without-spring")
include("pg-index-health-spring-boot-demo")

