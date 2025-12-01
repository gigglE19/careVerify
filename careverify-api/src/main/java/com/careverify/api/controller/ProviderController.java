package com.careverify.api.controller;

import com.careverify.cache.ProviderCache;
import com.careverify.common.model.ProviderEligibilityResponse;
import com.careverify.core.ProviderVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProviderController {
    private static final Logger log = LoggerFactory.getLogger(ProviderController.class);

    private final ProviderVerificationService verificationService;
    private final ProviderCache providerCache;

    public ProviderController(ProviderVerificationService verificationService, ProviderCache providerCache) {
        this.verificationService = verificationService;
        this.providerCache = providerCache;
    }

    @GetMapping(value = "/api/v1/providers/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProviderEligibilityResponse> verify(@RequestParam(name = "npi") String npi,
                                                     @RequestParam(name = "state", required = false) String state,
                                                     @RequestParam(name = "requiredSpecialty", required = false) String requiredSpecialty) {
        // Try cache first. If present, verify with normalized. If empty, call adapter and then verify.
        return providerCache.get(npi)
                .flatMap(cached -> Mono.deferContextual(ctx -> {
                    String cid = ctx.getOrDefault("correlationId", "");
                    log.info("Serving from CACHE for npi={} correlationId={}", npi, cid);
                    return verificationService.verifyWithNormalized(cached, state, requiredSpecialty);
                }))
                .switchIfEmpty(
                        Mono.deferContextual(ctx -> {
                            String cid = ctx.getOrDefault("correlationId", "");
                            log.info("Cache MISS for npi={} correlationId={}", npi, cid);
                            return verificationService.getNpiAdapterResult(npi)
                                    .flatMap(normalized -> {
                                        if (normalized != null) {
                                            providerCache.putAsync(npi, normalized, cid);
                                        }
                                        return verificationService.verifyWithNormalized(normalized, state, requiredSpecialty);
                                    })
                                    .defaultIfEmpty(ProviderEligibilityResponse.failed("NPI lookup failed"));
                        })
                );
    }
}
