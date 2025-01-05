rootProject.name = "pg-index-health-demo"

include("pg-index-health-demo-without-spring")
include("pg-index-health-spring-boot-demo")
include("db-migrations")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val springBoot3Version = version("spring-boot-v3", "3.4.1")
            plugin("spring-boot-v3", "org.springframework.boot")
                .versionRef(springBoot3Version)
            library("spring-boot-v3-dependencies", "org.springframework.boot", "spring-boot-dependencies")
                .versionRef(springBoot3Version)
        }
    }
}
