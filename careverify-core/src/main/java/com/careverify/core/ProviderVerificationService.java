package com.careverify.core;

import com.careverify.adapter.npi.NpiAdapter;
import com.careverify.adapter.npi.model.NpiNormalized;
import com.careverify.common.model.ProviderEligibilityResponse;
import com.careverify.common.model.ProviderInfo;
import com.careverify.common.model.ValidationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ProviderVerificationService {
    private static final Logger log = LoggerFactory.getLogger(ProviderVerificationService.class);
    private final NpiAdapter npiAdapter;

    public ProviderVerificationService(NpiAdapter npiAdapter) {
        this.npiAdapter = npiAdapter;
    }

    public Mono<ProviderEligibilityResponse> verify(String npi, String requiredState, String requiredSpecialty) {
        return npiAdapter.fetchByNpi(npi)
                .flatMap(raw -> verifyWithNormalized(raw, requiredState, requiredSpecialty))
                .defaultIfEmpty(ProviderEligibilityResponse.failed("NPI lookup failed"));
    }

    public Mono<NpiNormalized> getNpiAdapterResult(String npi) {
        return npiAdapter.fetchByNpi(npi).doOnNext(n -> {
            if (n == null) log.info("Adapter returned no data for {}", npi);
            else log.info("Adapter returned normalized for npi={} state={} status={}", npi, n.state(), n.status());
        });
    }

    public Mono<ProviderEligibilityResponse> verifyWithNormalized(NpiNormalized raw, String requiredState, String requiredSpecialty) {
        if (raw == null) {
            log.info("No data found for NPI (normalized input)");
            return Mono.just(ProviderEligibilityResponse.failed("NPI lookup returned no results"));
        }

        ProviderInfo provider = new ProviderInfo(raw.npi(), raw.name(), raw.state(), raw.specialties(), raw.status());
        boolean npiValid = true;
        boolean stateMatch = isStateMatch(provider.getState(), requiredState);
        boolean specialtyMatch = isSpecialtyMatch(provider.getSpecialties(), requiredSpecialty);

        ValidationDetails details = new ValidationDetails(npiValid, stateMatch, specialtyMatch);

        if (!stateMatch) {
            return Mono.just(ProviderEligibilityResponse.failed("Provider not licensed in required state", provider, details));
        }
        if (!specialtyMatch) {
            return Mono.just(ProviderEligibilityResponse.failed("Provider specialty mismatch", provider, details));
        }
        if (!"Active".equalsIgnoreCase(provider.getStatus())) {
            return Mono.just(ProviderEligibilityResponse.failed("Provider not active", provider, details));
        }
        return Mono.just(ProviderEligibilityResponse.success(provider, details));
    }

    private boolean isStateMatch(String providerState, String requiredState) {
        if (requiredState == null || requiredState.isBlank()) return true;
        if (providerState == null || providerState.isBlank()) return false;
        return providerState.equalsIgnoreCase(requiredState.trim());
    }

    private boolean isSpecialtyMatch(List<String> specialties, String requiredSpecialty) {
        if (requiredSpecialty == null || requiredSpecialty.isBlank()) return true;
        if (specialties == null || specialties.isEmpty()) return false;
        String req = requiredSpecialty.trim().toLowerCase();
        return specialties.stream().anyMatch(s -> s != null && s.toLowerCase().contains(req));
    }
}

