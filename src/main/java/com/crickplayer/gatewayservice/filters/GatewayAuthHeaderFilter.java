package com.crickplayer.gatewayservice.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayAuthHeaderFilter implements GlobalFilter, Ordered {

    // This secret key must be stored in the vault server.
    private static final String GATEWAY_SECRET = "gateway-secret-123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // If you want to skip this Header addition for auth uri, then you can add below condition.
        if (exchange.getRequest().getURI().getPath().startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-Gateway-Auth", GATEWAY_SECRET)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // Execute early, even before route, and security filter.
    }
}
