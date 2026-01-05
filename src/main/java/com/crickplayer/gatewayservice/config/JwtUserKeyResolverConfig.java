package com.crickplayer.gatewayservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Configuration
public class JwtUserKeyResolverConfig {

    // Limits per IP address
    /*
    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest()
                                .getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }*/

    // Limits Per user
    /*@Bean
    public KeyResolver keyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id"); // or extract from JWT
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }*/

    @Bean
    public KeyResolver jwtUserKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userId = extractUserIdFromJwt(token);
                return Mono.just(userId);
            }
            return Mono.just("anonymous");
        };
    }

    private String extractUserIdFromJwt(String token) {
        try {
            String payload = token.split("\\.")[1];
            String json = new String(Base64.getDecoder().decode(payload));
            // example: {"sub":"user123",...}
            return json.split("\"sub\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            return "invalid-user";
        }
    }
}

