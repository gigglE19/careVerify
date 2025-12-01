package com.careverify.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ServerLoggingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(ServerLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        return Mono.deferContextual(ctx -> {
            String ctxCid = ctx.getOrDefault("correlationId", "");
            String headerCid = req.getHeaders().getFirst("X-Correlation-Id");
            final String cid = (ctxCid != null && !ctxCid.isBlank()) ? ctxCid : (headerCid == null ? "" : headerCid);
            MDC.put("correlationId", cid);
            log.info("Server REQUEST method={} path={} query={} headers={} correlationId={}", req.getMethod(), req.getPath(), req.getQueryParams(), req.getHeaders(), cid);
            return chain.filter(exchange)
                    .doOnEach(sig -> {
                        if (sig.isOnNext() || sig.isOnError() || sig.isOnComplete()) {
                            MDC.put("correlationId", cid);
                        }
                    })
                    .doOnSuccess(v -> {
                        ServerHttpResponse resp = exchange.getResponse();
                        log.info("Server RESPONSE status={} path={} headers={} correlationId={}", resp.getStatusCode(), req.getPath(), resp.getHeaders(), cid);
                    })
                    .doFinally(sig -> MDC.remove("correlationId"));
        });
    }
}

