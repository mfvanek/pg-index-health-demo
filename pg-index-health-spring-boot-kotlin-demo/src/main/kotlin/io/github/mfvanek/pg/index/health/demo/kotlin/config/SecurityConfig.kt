/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Security configuration for the application.
 *
 * This configuration secures all HTTP endpoints with basic authentication and
 * configures CORS to allow requests from any origin. It is automatically
 * detected and applied by Spring Boot during application startup.
 *
 * The security credentials are configured in application.yaml:
 * spring.security.user.name and spring.security.user.password
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * This method sets up security rules that:
     * - Disable CSRF protection (suitable for stateless APIs)
     * - Require authentication for all requests
     * - Enable HTTP Basic authentication
     *
     * @param http the [HttpSecurity] to configure
     * @return configured [SecurityFilterChain] instance
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { obj: AbstractHttpConfigurer<*, HttpSecurity> -> obj.disable() } //NOSONAR
            .authorizeHttpRequests { authz -> authz.anyRequest().authenticated() }
            .httpBasic(Customizer.withDefaults())
            .build()
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     *
     * This method allows requests from any origin (*) to access the application's
     * endpoints. In production, this should be restricted to specific origins.
     *
     * @return configured [WebMvcConfigurer] instance
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return CorsConfigurer()
    }
    
    private class CorsConfigurer : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**").allowedOrigins("*") //NOSONAR
        }
    }
}
