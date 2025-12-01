package com.careverify.api.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

/**
 * Simple filter that attaches a correlation id for request tracing.
 */
@Configuration
public class WebConfig {
    public static final String CORRELATION_ID = "correlationId";

    @Bean
    public WebFilter correlationIdFilter() {
        return (exchange, chain) -> {
            String id = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
            if (id == null || id.isBlank()) {
                id = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID, id);
            return chain.filter(exchange).doFinally(signalType -> MDC.remove(CORRELATION_ID));
        };
    }
}
