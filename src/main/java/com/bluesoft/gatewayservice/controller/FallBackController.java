package com.bluesoft.gatewayservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallBackController {

    @GetMapping("/auth")
    public Mono<String> authFallback() {
        return Mono.just("Auth service is currently unavailable. Please try later.");
    }

    @GetMapping("/payments")
    public Mono<String> paymentFallback() {
        return Mono.just("Payment service is temporarily unavailable. Please retry.");
    }

    @GetMapping("/user")
    public Mono<String> globalFallback() {
        return Mono.just("Service temporarily unavailable.");
    }
}
