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
            val kotlinVersion = version("kotlin", "2.2.20")
            val osDetectorVersion = version("osdetector", "1.7.3")
            val httpClient5ParentVersion = version("httpclient5-parent", "5.5")
            val nettyAllVersion = version("netty-all", "4.2.4.Final")
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
            library("springdoc-openapi-bom", "org.springdoc", "springdoc-openapi-bom")
                .versionRef(springDocVersion)
            library("commons-lang3", "org.apache.commons", "commons-lang3")
                .versionRef(commonsLang3Version)
            library("httpclient5-parent", "org.apache.httpcomponents.client5", "httpclient5-parent")
                .versionRef(httpClient5ParentVersion)
            library("liquibase-core", "org.liquibase:liquibase-core:4.33.0")
            library("liquibase-sessionlock", "com.github.blagerweij:liquibase-sessionlock:1.6.9")
            library("netty-all", "io.netty", "netty-all")
                .versionRef(nettyAllVersion)
        }
    }
}
