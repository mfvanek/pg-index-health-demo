# pg-index-health-demo
Demo project for [pg-index-health](https://github.com/mfvanek/pg-index-health) library.
* With Embedded PostgreSQL
* Without Spring and Spring Boot

[![Java CI](https://github.com/mfvanek/pg-index-health-demo/workflows/Java%20CI/badge.svg)](https://github.com/mfvanek/pg-index-health-demo/actions "Java CI")
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/mfvanek/pg-index-health-demo/blob/master/LICENSE "Apache License 2.0")

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=bugs)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=mfvanek_pg-index-health-demo&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=mfvanek_pg-index-health-demo)

## Advantages
With **pg-index-health** library you will be able to:
1. [collect indexes health data](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/DemoApp.java) in production environment;
1. [analyze your database structure in functional tests](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/test/java/io/github/mfvanek/pg/index/health/demo/IndexesMaintenanceTest.java) and prevent many of typical errors;
1. [analyze your database configuration](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/ConfigurationDemoApp.java) to prevent using of default values for important options;
1. [manage your database statistics](https://github.com/mfvanek/pg-index-health-demo/blob/master/src/main/java/io/github/mfvanek/pg/index/health/demo/StatisticsDemoApp.java) in production environment.
