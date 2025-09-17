rootProject.name = "pg-index-health-demo"

include("pg-index-health-demo-without-spring")
include("pg-index-health-spring-boot-demo")
include("pg-index-health-spring-boot-kotlin-demo")
include("db-migrations")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val springDocVersion = version("springdoc-openapi", "2.8.13")
            val commonsLang3Version = version("commons-lang3", "3.18.0")
            val springBoot3Version = version("spring-boot-v3", "3.5.5")
            plugin("spring-boot-v3", "org.springframework.boot")
                .versionRef(springBoot3Version)
            library("spring-boot-v3-dependencies", "org.springframework.boot", "spring-boot-dependencies")
                .versionRef(springBoot3Version)
            library("springdoc-openapi-bom", "org.springdoc", "springdoc-openapi-bom")
                .versionRef(springDocVersion)
            library("commons-lang3", "org.apache.commons", "commons-lang3")
                .versionRef(commonsLang3Version)
            library("liquibase-core", "org.liquibase:liquibase-core:4.33.0")
            library("liquibase-sessionlock", "com.github.blagerweij:liquibase-sessionlock:1.6.9")
        }
    }
}
