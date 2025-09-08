/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class DefaultController(
    @Value("\${server.port}")
    private val port: Int,
    
    @Value("\${management.server.port}")
    private val actuatorPort: Int
) {
    
    /**
     * Redirects root request to Swagger UI.
     *
     * @param request incoming HttpServletRequest
     * @param response HttpServletResponse for redirect
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/")
    @Throws(IOException::class)
    fun redirect(request: HttpServletRequest, response: HttpServletResponse) {
        val requestUrl = request.requestURL.toString()
        val targetUrl = requestUrl.replace(port.toString(), actuatorPort.toString()) + "actuator/swagger-ui"
        logger.info("Redirecting to {}", targetUrl)
        response.sendRedirect(targetUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultController::class.java)
    }
}
