/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

plugins {
    id("java")
    id("jacoco")
    id("org.gradle.test-retry")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("--should-stop=ifError=FLOW")
    }

    test {
        useJUnitPlatform()
        maxParallelForks = 1
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification)

        retry {
            maxRetries.set(1)
            maxFailures.set(3)
            failOnPassedAfterRetry.set(false)
        }
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
        violationRules {
            rule {
                limit {
                    counter = "CLASS"
                    value = "MISSEDCOUNT"
                    maximum = "0.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "METHOD"
                    value = "MISSEDCOUNT"
                    maximum = "0.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "LINE"
                    value = "MISSEDCOUNT"
                    maximum = "1.0".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.92".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.7".toBigDecimal()
                }
            }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}

jacoco {
    toolVersion = "0.8.13"
}
