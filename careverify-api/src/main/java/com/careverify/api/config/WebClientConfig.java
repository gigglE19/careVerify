package com.careverify.api.config;

import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient.Builder apiWebClientBuilder() {
        AddressResolverGroup<?> resolverGroup = DefaultAddressResolverGroup.INSTANCE;
        HttpClient httpClient = HttpClient.create().resolver(resolverGroup).responseTimeout(Duration.ofSeconds(10));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .clientConnector(connector)
                .filter(addCorrelationHeaderFilter())
                .filter(logRequest())
                .filter(logResponse());
    }

    private ExchangeFilterFunction addCorrelationHeaderFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> Mono.deferContextual(ctx -> {
            String correlation = ctx.getOrDefault(CorrelationIdWebFilter.MDC_KEY, "");
            if (correlation != null && !correlation.isBlank()) {
                ClientRequest newReq = ClientRequest.from(req).header(CorrelationIdWebFilter.CORRELATION_ID_HEADER, correlation).build();
                return Mono.just(newReq);
            }
            return Mono.just(req);
        }));
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> Mono.deferContextual(ctx -> {
            String cid = ctx.getOrDefault(CorrelationIdWebFilter.MDC_KEY, "");
            log.info("WebClient REQUEST method={} url={} headers={} correlationId={}", req.method(), req.url(), req.headers(), cid);
            return Mono.just(req);
        }));
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> Mono.deferContextual(ctx -> {
            String cid = ctx.getOrDefault(CorrelationIdWebFilter.MDC_KEY, "");
            log.info("WebClient RESPONSE status={} headers={} correlationId={}", resp.statusCode(), resp.headers().asHttpHeaders(), cid);
            return Mono.just(resp);
        }));
    }
}

