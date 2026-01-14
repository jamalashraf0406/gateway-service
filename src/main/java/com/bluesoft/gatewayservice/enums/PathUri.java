package com.bluesoft.gatewayservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathUri {
    LOGIN_URI("/auth/login"),
    JWKS_URI("/auth/refresh/token"),
    TOKEN_REFRESH_URI("/.well-known/jwks.json"),
    AUTH_PATTERN("/auth/**"),
    USER_PATTERN("/user/**"),
    CREATE_USER_URI("/user/v1/create"),

    VIDEO_PATTERN("/video/**");

    private final String uri;

}
