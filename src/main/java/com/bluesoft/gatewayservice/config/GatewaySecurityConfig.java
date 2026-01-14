package com.bluesoft.gatewayservice.config;

import com.bluesoft.gatewayservice.enums.PathUri;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Flux;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    /**
     * Auth endpoints not protected — NO JWT decoding or required, gateway let the request forward to auth service
     * without checking JWT decoding
    */
    @Bean
    @Order(1)
    public SecurityWebFilterChain authChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        PathUri.AUTH_PATTERN.getUri(), "/saga/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }

    // Protected APIs — JWT required, as below apis are protected so authorization header mut be validated.
    @Bean
    @Order(2)
    public SecurityWebFilterChain apiChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverter jwtAuthConverter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(PathUri.USER_PATTERN.getUri())
                            .hasAnyRole("ROLE_USER", "ROLE_ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }

    /*
    This will decode the JWT token in case of refresh token as well, as Refresh token request will have authorization
    Header so gateway will try to decode and validate JWT token. as below method has this line of code

    oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(PathUri.AUTH_PATTERN.getUri()).permitAll()
                        .pathMatchers(PathUri.USER_PATTERN.getUri()).hasAnyRole("ROLE_USER", "ROLE_ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }*/

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null || roles.isEmpty()) {
                return Flux.empty();
            }
            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }
}

