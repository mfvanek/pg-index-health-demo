rootProject.name = "pg-index-health-demo"

include("pg-index-health-demo-without-spring")
include("pg-index-health-spring-boot-demo")
include("pg-index-health-spring-boot-kotlin-demo")
include("db-migrations")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val springBoot3Version = version("spring-boot-v3", "3.5.6")
            val kotlinVersion = version("kotlin", "2.0.21")
            val osDetectorVersion = version("osdetector", "1.7.3")
            val detektVersion = version("detekt", "1.23.8")

            plugin("spring-boot-v3", "org.springframework.boot")
                .versionRef(springBoot3Version)
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm")
                .versionRef(kotlinVersion)
            plugin("kotlin-spring", "org.jetbrains.kotlin.plugin.spring")
                .versionRef(kotlinVersion)
            plugin("osdetector", "com.google.osdetector")
                .versionRef(osDetectorVersion)
            plugin("detekt", "io.gitlab.arturbosch.detekt")
                .versionRef(detektVersion)
            library("spring-boot-v3-dependencies", "org.springframework.boot", "spring-boot-dependencies")
                .versionRef(springBoot3Version)
            library("springdoc-openapi-bom", "org.springdoc:springdoc-openapi-bom:2.8.13")
            library("commons-lang3", "org.apache.commons:commons-lang3:3.19.0")
            library("liquibase-core", "org.liquibase:liquibase-core:5.0.0")
            library("liquibase-sessionlock", "com.github.blagerweij:liquibase-sessionlock:1.6.9")
            library("netty-all", "io.netty:netty-all:4.2.4.Final")
            library("kotlin-logging", "io.github.oshai:kotlin-logging-jvm:7.0.3")
        }
    }
}
