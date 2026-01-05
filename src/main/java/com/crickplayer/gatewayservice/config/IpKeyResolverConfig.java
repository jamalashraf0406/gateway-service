package com.crickplayer.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Configuration
public class IpKeyResolverConfig {

    @Bean("ipKeyResolver")
    @Primary
    public KeyResolver ipKeyResolver() {
        log.info("****************************************************************");
        log.info("********************** Getting IP key resolver *****************");
        log.info("****************************************************************");
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest()
                                .getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }
}
