package com.crickplayer.gatewayservice.config;

import io.lettuce.core.resource.ClientResources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;
    private final Environment environment;

    /**
     * Shared client resources (Netty event loops, threads)
     * Single instance per JVM – IMPORTANT for performance
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return ClientResources.create();
    }

    /**
     * Reactive Redis Connection Factory (Lettuce + Pooling)
     */
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(
            ClientResources clientResources) {

        // ---- Pool Configuration ----
        GenericObjectPoolConfig<String> poolConfig = getStringGenericObjectPoolConfig();

        // ---- Redis Node Configuration ----
        System.out.println("PHost: "+ redisProperties.getHost() +"Port: "+redisProperties.getPort());
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
                environment.getProperty("spring.redis.host"),
                Integer.valueOf(environment.getProperty("spring.redis.port"))
        );

        redisConfig.setDatabase(redisProperties.getDatabase());

        if (redisProperties.getPassword() != null) {
            redisConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        // ---- Client Configuration ----
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder()
                        .poolConfig(poolConfig)
                        .commandTimeout(
                                Optional.ofNullable(redisProperties.getTimeout())
                                        .orElse(Duration.ofSeconds(2))
                        )
                        .shutdownTimeout(
                                Optional.ofNullable(redisProperties.getLettuce().getShutdownTimeout())
                                        .orElse(Duration.ofMillis(100))
                        )
                        .clientResources(clientResources)
                        .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    private GenericObjectPoolConfig<String> getStringGenericObjectPoolConfig() {
        GenericObjectPoolConfig<String> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxWait(
                Optional.ofNullable(redisProperties.getLettuce().getPool().getMaxWait())
                        .orElse(Duration.ofSeconds(5))
        );
        return poolConfig;
    }

    /**
     * Reactive Redis Template (String-based)
     * REQUIRED by RedisRateLimiter
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        return new ReactiveStringRedisTemplate(factory);
    }

    /**
     * Gateway Redis Rate Limiter
     * Example:
     *  - 10 req/sec
     *  - Burst up to 20
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public ApplicationRunner redisDebug() {
        return args -> {
            System.out.println(">>> spring.redis.host = " + environment.getProperty("spring.redis.host"));
            System.out.println(">>> spring.redis.port = " + environment.getProperty("spring.redis.port"));
        };
    }

    @Bean
    public ApplicationRunner redisFactoryDeepDebug(
            ReactiveRedisConnectionFactory factory) {

        return args -> {
            if (factory instanceof LettuceConnectionFactory lcf) {
                System.out.println(">>> Redis host used = " + lcf.getHostName());
                System.out.println(">>> Redis port used = " + lcf.getPort());
            }
        };
    }
}
