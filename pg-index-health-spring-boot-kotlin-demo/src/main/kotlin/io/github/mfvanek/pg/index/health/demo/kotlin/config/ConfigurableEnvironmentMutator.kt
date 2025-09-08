/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.MutablePropertySources
import org.testcontainers.containers.JdbcDatabaseContainer

/**
 * Utility object for dynamically configuring the Spring application environment.
 *
 * This object is used to add datasource configuration properties to the Spring environment
 * when running with Testcontainers. It ensures that the datasource URL is properly set
 * to point to the Testcontainers-managed database instance when no explicit datasource
 * configuration is provided.
 *
 * Primary use case is with Testcontainers integration in [DatabaseConfig] to enable
 * seamless testing with ephemeral database instances.
 *
 * TODO: move to test directory?
 *
 * @see DatabaseConfig
 * @see JdbcDatabaseContainer
 */
object ConfigurableEnvironmentMutator {

    /**
     * The name of the Spring datasource URL property.
     */
    const val DATASOURCE_URL_PROP_NAME = "spring.datasource.url"

    /**
     * Adds the datasource URL to the Spring environment if it's not already configured.
     *
     * This method checks if the datasource URL property is already set in the environment.
     * If not, and if the environment is configurable, it adds a property source with
     * the JDBC URL from the Testcontainers database container.
     *
     * Used in [DatabaseConfig] to support Testcontainers-based integration testing
     * by dynamically setting the datasource URL to point to the Testcontainers instance.
     *
     * @param jdbcDatabaseContainer the Testcontainers JDBC database container
     * @param environment the Spring environment
     * @return true if the datasource URL was added, false otherwise
     */
    fun addDatasourceUrlIfNeed(
        jdbcDatabaseContainer: JdbcDatabaseContainer<*>,
        environment: Environment
    ): Boolean {
        if (environment.getProperty(DATASOURCE_URL_PROP_NAME) == null &&
            environment is ConfigurableEnvironment
        ) {
            val mps: MutablePropertySources = environment.propertySources
            mps.addFirst(
                MapPropertySource(
                    "connectionString",
                    mapOf(DATASOURCE_URL_PROP_NAME to jdbcDatabaseContainer.jdbcUrl)
                )
            )
            return true
        }
        return false
    }
}
