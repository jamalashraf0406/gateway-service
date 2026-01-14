package com.bluesoft.gatewayservice.filters;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisDebugLogger {

    public RedisDebugLogger(RedisProperties props) {
        log.info("Redis Host = {}", props.getHost());
        log.info("Redis Port = {}", props.getPort());
    }

    @PostConstruct
    public void printConfigLocation() {
        log.info("Loaded from: {}", getClass().getClassLoader().getResource("application.yaml"));
    }

    @PostConstruct
    public void checkEnv() {
        System.out.println("ENV spring.redis.host = " + System.getenv("SPRING_REDIS_HOST"));
        System.out.println("SYS spring.redis.host = " + System.getProperty("spring.redis.host"));

    }
}

