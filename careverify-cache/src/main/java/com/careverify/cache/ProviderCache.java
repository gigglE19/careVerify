package com.careverify.cache;

import com.careverify.adapter.npi.NpiAdapter;
import com.careverify.adapter.npi.model.NpiNormalized;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Component
public class ProviderCache {
    private static final Logger log = LoggerFactory.getLogger(ProviderCache.class);
    private final Cache<String, NpiNormalized> cache;
    private final NpiAdapter npiAdapter;

    public ProviderCache(NpiAdapter npiAdapter) {
        this.npiAdapter = npiAdapter;
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(120)).maximumSize(20000).build();
    }

    public Mono<NpiNormalized> get(String npi) {
        NpiNormalized present = cache.getIfPresent(npi);
        if (present != null) {
            return Mono.just(present);
        }
        return Mono.empty();
    }

    public void putAsync(String npi, NpiNormalized value, String correlationId) {
        log.info("ProviderCache.putAsync scheduled for {} correlationId={}", npi, correlationId);
        Mono.fromRunnable(() -> {
            cache.put(npi, value);
            log.info("ProviderCache.putAsync - cache updated for {} correlationId={}", npi, correlationId);
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public void invalidate(String npi) {
        cache.invalidate(npi);
    }
}

