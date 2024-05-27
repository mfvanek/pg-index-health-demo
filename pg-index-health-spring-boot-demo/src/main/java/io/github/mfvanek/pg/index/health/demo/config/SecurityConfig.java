/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import lombok.SneakyThrows;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    @SneakyThrows
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) {
        httpSecurity.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests(requests -> requests.anyRequest().authenticated())
            .httpBasic();
        return httpSecurity.build();
    }

    @Nonnull
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@Nonnull final CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*"); //NOSONAR
            }
        };
    }
}
