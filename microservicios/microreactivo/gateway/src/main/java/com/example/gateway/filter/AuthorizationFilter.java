package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Gateway filter that validates user authorization based on headers and endpoint permissions.
 *
 * Expected headers:
 * - X-User-Id: User identifier
 * - X-User-Role: User role ID (1=ADMIN, 2=PROPERTY_OWNER, 3=USER)
 * - X-User-Email: User email (optional)
 */
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract headers
            String userId = getHeader(request, "X-User-Id");
            String userRoleStr = getHeader(request, "X-User-Role");
            String path = request.getPath().toString();
            String method = request.getMethod().toString();

            // Validate required headers
            if (userId == null || userId.isEmpty()) {
                return unauthorized(exchange, "Missing X-User-Id header");
            }

            if (userRoleStr == null || userRoleStr.isEmpty()) {
                return unauthorized(exchange, "Missing X-User-Role header");
            }

            // Parse and validate role ID
            Integer roleId;
            try {
                roleId = Integer.parseInt(userRoleStr);
            } catch (NumberFormatException e) {
                return unauthorized(exchange, "Invalid role format: " + userRoleStr);
            }

            if (!isValidRole(roleId)) {
                return unauthorized(exchange, "Invalid role ID: " + roleId);
            }

            // Check permissions based on role and endpoint
            if (!hasPermission(roleId, method, path, userId)) {
                return forbidden(exchange, "Insufficient permissions for " + method + " " + path);
            }

            // Add user context to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", userRoleStr)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        List<String> headers = request.getHeaders().get(headerName);
        return (headers != null && !headers.isEmpty()) ? headers.get(0) : null;
    }

    private boolean isValidRole(Integer roleId) {
        // Valid role IDs: 1=ADMIN, 2=PROPERTY_OWNER, 3=USER
        return roleId != null && roleId >= 1 && roleId <= 3;
    }

    private boolean hasPermission(Integer roleId, String method, String path, String userId) {
        // ADMIN (roleId=1) can do everything
        if (roleId == 1) {
            return true;
        }

        // PROPERTY_OWNER (roleId=2) permissions
        if (roleId == 2) {
            return hasPropertyOwnerPermission(method, path);
        }

        // USER (roleId=3) permissions (most restrictive)
        if (roleId == 3) {
            return hasUserPermission(method, path);
        }

        return false;
    }

    private boolean hasPropertyOwnerPermission(String method, String path) {
        // Property owners can manage motels, rooms, and services
        if (path.startsWith("/api/motels")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        if (path.startsWith("/api/rooms")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        if (path.startsWith("/api/services")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        // Property owners can view all bookings (to manage their properties)
        if (path.startsWith("/api/bookings")) {
            return method.equals("GET") || method.equals("PUT"); // Can confirm/cancel
        }

        return false;
    }

    private boolean hasUserPermission(String method, String path) {
        // Users can only read motels, rooms, and services
        if (path.startsWith("/api/motels") ||
            path.startsWith("/api/rooms") ||
            path.startsWith("/api/services")) {
            return method.equals("GET");
        }

        // Users can create and manage their own bookings
        if (path.startsWith("/api/bookings")) {
            // Allow GET for their own bookings and POST to create
            return method.equals("GET") || method.equals("POST") || method.equals("PUT");
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Error-Message", message);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("X-Error-Message", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
