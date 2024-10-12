/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class DefaultController {

    @Value("${server.port}")
    private int port;

    @Value("${management.server.port}")
    private int actuatorPort;

    @GetMapping("/")
    public void redirect(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String requestUrl = request.getRequestURL().toString();
        final String targetUrl = requestUrl.replace(String.valueOf(port), String.valueOf(actuatorPort)) + "actuator/swagger-ui";
        log.info("Redirecting to {}", targetUrl);
        response.sendRedirect(targetUrl);
    }
}
