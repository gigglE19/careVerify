package com.careverify.api.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String existing = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = existing != null && !existing.isBlank() ? existing : UUID.randomUUID().toString();
        MDC.put(MDC_KEY, correlationId);
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(MDC_KEY, correlationId))
                .doOnEach(sig -> {
                    if (sig.isOnNext() || sig.isOnError() || sig.isOnComplete()) {
                        MDC.put(MDC_KEY, correlationId);
                    }
                })
                .doFinally(st -> MDC.remove(MDC_KEY));
    }
}

