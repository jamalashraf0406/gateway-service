package com.crickplayer.gatewayservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceDetails {
    AUTH("auth-service", "http://localhost:8082"),
    VIDEO("video-service", "http://localhost:8080");

    private final String name;
    private final String url;
}
