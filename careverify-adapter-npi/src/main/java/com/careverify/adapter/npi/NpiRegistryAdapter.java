package com.careverify.adapter.npi;

import com.careverify.adapter.npi.model.NpiNormalized;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete adapter that calls the public NPI Registry and converts response
 * into NpiNormalized. Uses reactive WebClient and a simple fallback strategy.
 */
@Component
public class NpiRegistryAdapter implements NpiAdapter {
    private static final Logger log = Logger.getLogger(NpiRegistryAdapter.class.getName());
    private final WebClient client;

    public NpiRegistryAdapter(@Qualifier("apiWebClientBuilder") WebClient.Builder builder) {
        this.client = builder.baseUrl("https://npiregistry.cms.hhs.gov/api/").build();
    }

    @Override
    public Mono<NpiNormalized> fetchByNpi(String npi) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("number", npi)
                        .queryParam("version", "2.1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(NpiRegistryResponse.class)
                .timeout(Duration.ofSeconds(4))
                .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(250)))
                .map(resp -> {
                    if (resp == null || resp.results == null || resp.results.isEmpty()) {
                        log.info("NPI registry returned no results for " + npi);
                        return null;
                    }
                    NpiResult r = resp.results.get(0);
                    String name = r.basic != null ? r.basic.name : null;
                    String state = r.addresses != null && !r.addresses.isEmpty() ? r.addresses.get(0).state : null;
                    List<String> specs = r.taxonomies == null ? Collections.emptyList()
                            : r.taxonomies.stream().map(t -> t.desc == null ? "" : t.desc).collect(Collectors.toList());
                    String status = r.basic != null && "A".equalsIgnoreCase(r.basic.status) ? "Active" : "Inactive";
                    return new NpiNormalized(npi, tidyName(name), tidyState(state), specs, status);
                })
                .doOnSuccess(v -> log.fine("Fetched NPI " + npi + " -> " + v))
                .onErrorResume(ex -> {
                    log.log(Level.WARNING, "Failed to fetch NPI " + npi + ": " + ex.toString());
                    return Mono.empty();
                });
    }

    private static String tidyName(String raw) {
        if (raw == null) return null;
        String[] parts = raw.trim().toLowerCase().split("\\s+");
        return String.join(" ", java.util.Arrays.stream(parts)
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).toList());
    }

    private static String tidyState(String state) {
        if (state == null) return null;
        return state.trim().toUpperCase();
    }

    // Narrow set of fields from the NPI response that we need
    public static class NpiRegistryResponse {
        public java.util.List<NpiResult> results;
    }

    public static class NpiResult {
        public Basic basic;
        public java.util.List<Taxonomy> taxonomies;
        public java.util.List<Address> addresses;
    }

    public static class Basic {
        public String name;
        public String enumeration_date;
        public String status;
    }

    public static class Taxonomy {
        public String code;
        public String desc;
        public boolean primary;
    }

    public static class Address {
        public String address_purpose;
        public String city;
        public String state;
        public String postal_code;
    }
}
