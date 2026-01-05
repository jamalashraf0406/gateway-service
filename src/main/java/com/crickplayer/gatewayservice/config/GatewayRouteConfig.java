package com.crickplayer.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayRouteConfig {

    String architecture = """
            Client
              ↓
            Spring Cloud Gateway
              ├─ Global Filters
              │    ├─ CorrelationId
              │    ├─ Logging
              │    └─ Header propagation
              │
              ├─ Rate Limiter (Redis)        ✅ ALL APIs
              ├─ Circuit Breaker (Resilience4j)
              │
              ├─ Security (JWT Validation)
              │    └─ Fetch public key from Auth Service (JWKS)
              │
              ↓
            Microservices
              ├─ Auth Service
              │    └─ /auth/**, /oauth2/jwks
              └─ User Service
                   └─ /user/v1/create
            
            """;

    private final RedisRateLimiter rateLimiter;
    private final KeyResolver ipKeyResolver;
    private final KeyResolver jwtUserKeyResolver;

    public GatewayRouteConfig(
            RedisRateLimiter rateLimiter,
            @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver,
            @Qualifier("jwtUserKeyResolver") KeyResolver jwtUserKeyResolver) {

        this.rateLimiter = rateLimiter;
        this.ipKeyResolver = ipKeyResolver;
        this.jwtUserKeyResolver = jwtUserKeyResolver;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-public", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(rateLimiter);
                                    c.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("http://localhost:8082")
                )

                .route("saga-service", r -> r
                        .path("/saga/**")
                        .filters(f -> f
                                .filter((exchange, chain) -> {
                                    log.info("************************* CHECKING ******************");
                                    log.info("Gateway route matched: {}" , exchange.getRequest().getPath());
                                    return chain.filter(exchange);
                                })
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(rateLimiter);
                                    c.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("http://localhost:5050")
                )

                .route("user-create", r -> r
                        .path("/user/v1/create")
                        .filters(f -> f
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(rateLimiter);
                                    c.setKeyResolver(jwtUserKeyResolver);
                                })
                        )
                        .uri("http://localhost:8082")
                )

                .route("user-protected", r -> r
                        .path("/user/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(rateLimiter);
                                    c.setKeyResolver(jwtUserKeyResolver);
                                })
                        )
                        .uri("lb://USER-SERVICE")
                )

                .build();
    }

}