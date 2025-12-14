package com.example.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for AuthorizationFilter
 * Tests role-based authorization for motel creation and other endpoints
 */
class AuthorizationFilterTest {

    private AuthorizationFilter authorizationFilter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        authorizationFilter = new AuthorizationFilter();
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowAdminToCreateMotel() {
        // Admin (roleId=1) should be able to POST to /api/motels
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "admin-user-1")
                .header("X-User-Role", "1")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should not set error status
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowPropertyOwnerToCreateMotel() {
        // Property Owner (roleId=2) should be able to POST to /api/motels
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "owner-user-1")
                .header("X-User-Role", "2")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should not set error status
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyUserToCreateMotel() {
        // Regular User (roleId=3) should NOT be able to POST to /api/motels
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "regular-user-1")
                .header("X-User-Role", "3")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 403 Forbidden
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowUserToReadMotels() {
        // Regular User (roleId=3) should be able to GET /api/motels
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/api/motels")
                .header("X-User-Id", "regular-user-1")
                .header("X-User-Role", "3")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should not set error status
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithMissingUserId() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Role", "2")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithMissingRole() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "user-1")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithInvalidRoleFormat() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "ADMIN")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithInvalidRoleId() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/motels")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "99")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowAdminToAccessAllEndpoints() {
        // Admin should have access to any endpoint
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.DELETE, "/api/rooms/123")
                .header("X-User-Id", "admin-user-1")
                .header("X-User-Role", "1")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should not set error status
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowPropertyOwnerToManageRooms() {
        // Property Owner should be able to POST to /api/rooms
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/rooms")
                .header("X-User-Id", "owner-user-1")
                .header("X-User-Role", "2")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should not set error status
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyUserToCreateRooms() {
        // Regular User should NOT be able to POST to /api/rooms
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/rooms")
                .header("X-User-Id", "regular-user-1")
                .header("X-User-Role", "3")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = authorizationFilter.apply(new AuthorizationFilter.Config())
                .filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Should return 403 Forbidden
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }
}
