package com.rideease.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ensures strict port isolation while permitting universal access for:
 * - REST APIs (/api/**)
 * - WebSocket streaming (/ws-ride/**)
 * - Static frontend assets (/css/**, /js/**, /images/**)
 * 
 * Port 8080 -> User / Passenger Portal & Universal REST API Gateway
 * Port 8081 -> Driver Portal only
 * Port 8082 -> Admin Portal only
 */
@Component
public class PortAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        int localPort = req.getLocalPort();
        String path = req.getRequestURI();

        // 1. Always permit static assets, universal REST APIs, and WebSocket broker
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
            path.startsWith("/favicon.ico") || path.startsWith("/api/") || path.startsWith("/ws-ride")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Port 8080: Passenger / User Portal - Block direct /driver/** and /admin/**
        if (localPort == 8080) {
            if (path.startsWith("/driver/")) {
                res.sendRedirect("http://localhost:8081/driver/login");
                return;
            }
            if (path.startsWith("/admin/")) {
                res.sendRedirect("http://localhost:8082/admin/login");
                return;
            }
        }

        // 3. Port 8081: Driver Portal - Block /user/** and /admin/**
        if (localPort == 8081) {
            if (path.equals("/") || path.startsWith("/user/")) {
                res.sendRedirect("/driver/dashboard");
                return;
            }
            if (path.startsWith("/admin/")) {
                res.sendRedirect("http://localhost:8082/admin/login");
                return;
            }
        }

        // 4. Port 8082: Admin Portal - Block /user/** and /driver/**
        if (localPort == 8082) {
            if (path.equals("/") || path.startsWith("/user/")) {
                res.sendRedirect("/admin/dashboard");
                return;
            }
            if (path.startsWith("/driver/")) {
                res.sendRedirect("http://localhost:8081/driver/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
