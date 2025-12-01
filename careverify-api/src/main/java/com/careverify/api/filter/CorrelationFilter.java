package com.careverify.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Server side filter that ensures correlation id is present on incoming requests and
 * is propagated in Reactor context for downstream processing and outgoing WebClient calls.
 */
@Component
public class CorrelationFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(CorrelationFilter.class);
    private static final String HEADER = "X-Correlation-ID";
    private static final String CTX_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String incoming = request.getHeaders().getFirst(HEADER);
        String cid = (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;
        exchange.getResponse().getHeaders().add(HEADER, cid);
        log.info("Incoming request {} {} correlationId={}", request.getMethod(), request.getPath(), cid);
        return chain.filter(exchange).contextWrite(ctx -> ctx.put(CTX_KEY, cid));
    }
}

