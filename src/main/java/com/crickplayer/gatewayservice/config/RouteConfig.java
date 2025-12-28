package com.crickplayer.gatewayservice.config;

import com.crickplayer.gatewayservice.enums.PathUri;
import com.crickplayer.gatewayservice.enums.ServiceDetails;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                .route(ServiceDetails.AUTH.getName(), r -> r
                        .path(
                                PathUri.AUTH_PATTERN.getUri(),
                                PathUri.USER_PATTERN.getUri()
                        )
                        .uri(ServiceDetails.AUTH.getUrl()))

                .route(ServiceDetails.VIDEO.getName(), r -> r
                        .path(PathUri.VIDEO_PATTERN.getUri())
                        .uri(ServiceDetails.VIDEO.getUrl()))

                .build();
    }
}

