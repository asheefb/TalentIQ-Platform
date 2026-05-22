package com.asheef.api_gateway.filter;

import com.asheef.api_gateway.config.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * A named GatewayFilterFactory — referenced in application.yaml as `JwtAuthGatewayFilter`.
 * Rejects requests that do not carry a valid Bearer JWT, and propagates user info
 * to downstream services via headers so they don't need to re-parse the token.
 */
@Component("JwtAuthGateway")
public class JwtAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilterFactory.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return unauthorized(exchange.getResponse(), "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtUtil.parse(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                ServerHttpRequest mutated = request.mutate()
                        .header("X-Auth-User", username == null ? "" : username)
                        .header("X-Auth-Role", role == null ? "" : role)
                        .build();

                log.debug("Authenticated request to {} as user={}, role={}",
                        request.getPath(), username, role);

                return chain.filter(exchange.mutate().request(mutated).build());
            } catch (Exception e) {
                log.warn("JWT validation failed for path={} reason={}", request.getPath(), e.getMessage());
                return unauthorized(exchange.getResponse(), "Invalid or expired token");
            }
        };
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String reason) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("X-Auth-Error", reason);
        return response.setComplete();
    }

    /** Placeholder config for future per-route tuning (e.g., role requirements). */
    public static class Config {
    }
}
