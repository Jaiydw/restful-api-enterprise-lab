package com.example.restlab.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Structured Request Logging Filter (Experiment 2.1.2)
 * Intercepts requests, logs HTTP method, endpoint, and captures execution duration in ms.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();

        log.info("Incoming Request -> [{} {}] from IP: {}", method, uri, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 400) {
                log.warn("Outgoing Response <- [{} {}] Status: {} | Duration: {} ms", method, uri, status, duration);
            } else {
                log.info("Outgoing Response <- [{} {}] Status: {} | Duration: {} ms", method, uri, status, duration);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console") || path.startsWith("/actuator");
    }
}
